package com.paybridge.testing

import com.paybridge.data.local.dao.ClaimDao
import com.paybridge.data.local.dao.IncomingNotificationDao
import com.paybridge.data.local.dao.ListenerHeartbeatDao
import com.paybridge.data.local.dao.UnparsedNotificationDao
import com.paybridge.data.local.entity.ClaimEntity
import com.paybridge.data.local.entity.IncomingNotificationEntity
import com.paybridge.data.local.entity.ListenerHeartbeatEntity
import com.paybridge.data.local.entity.UnparsedNotificationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory fakes standing in for Room in unit tests. These are plain lists rather than a mock
 * of the SQL, which is deliberate: MatchingEngine's own filtering logic is what's under test in
 * MatchingEngineTest, while the SQL queries themselves are covered separately by instrumented
 * Room DAO tests (see /app/src/androidTest). Fakes here re-implement just enough of each query's
 * documented behavior to exercise that logic.
 */
class FakeClaimDao : ClaimDao {
    private val claims = MutableStateFlow<List<ClaimEntity>>(emptyList())
    private var nextId = 1L

    override suspend fun insert(claim: ClaimEntity): Long {
        val withId = claim.copy(id = nextId++)
        claims.value = claims.value + withId
        return withId.id
    }

    override suspend fun update(claim: ClaimEntity) {
        claims.value = claims.value.map { if (it.id == claim.id) claim else it }
    }

    override suspend fun getById(id: Long): ClaimEntity? = claims.value.find { it.id == id }

    override fun observeById(id: Long): Flow<ClaimEntity?> = claims.map { list -> list.find { it.id == id } }

    override fun observeAll(): Flow<List<ClaimEntity>> = claims

    override suspend fun getPendingOldestFirst(): List<ClaimEntity> =
        claims.value.filter { it.status == "PENDING" }.sortedBy { it.createdAt }

    override suspend fun findExactMatchCandidates(amount: Long, provider: String, receivedAt: Long): List<ClaimEntity> =
        claims.value.filter {
            it.status == "PENDING" &&
                it.expectedAmount == amount &&
                (it.expectedProvider == null || it.expectedProvider == provider) &&
                receivedAt in it.createdAt..it.timeoutAt
        }.sortedBy { it.createdAt }

    override suspend fun findProviderScopedPending(provider: String, receivedAt: Long): List<ClaimEntity> =
        claims.value.filter {
            it.status == "PENDING" &&
                (it.expectedProvider == null || it.expectedProvider == provider) &&
                receivedAt in it.createdAt..it.timeoutAt
        }.sortedBy { it.createdAt }

    override suspend fun getPendingPastTimeout(cutoff: Long): List<ClaimEntity> =
        claims.value.filter { it.status == "PENDING" && it.timeoutAt <= cutoff }

    fun all(): List<ClaimEntity> = claims.value
}

class FakeIncomingNotificationDao : IncomingNotificationDao {
    private val rows = mutableListOf<IncomingNotificationEntity>()
    private var nextId = 1L

    override suspend fun insert(notification: IncomingNotificationEntity): Long {
        val withId = notification.copy(id = nextId++)
        rows += withId
        return withId.id
    }

    override suspend fun markConsumed(id: Long, claimId: Long) {
        val idx = rows.indexOfFirst { it.id == id }
        if (idx >= 0) rows[idx] = rows[idx].copy(consumedByClaimId = claimId)
    }

    override suspend fun findUnconsumedMatching(
        amount: Long,
        claimProvider: String?,
        windowStart: Long,
        windowEnd: Long,
    ): List<IncomingNotificationEntity> =
        rows.filter {
            it.consumedByClaimId == null &&
                it.amount == amount &&
                (claimProvider == null || it.provider == claimProvider) &&
                it.receivedAt in windowStart..windowEnd
        }.sortedBy { it.receivedAt }

    override suspend fun existsWithDedupeKeyWithinBucket(key: String, bucketStart: Long, bucketEnd: Long): Boolean =
        rows.any { it.dedupeKey == key && it.receivedAt in bucketStart..bucketEnd }

    fun all(): List<IncomingNotificationEntity> = rows.toList()
}

class FakeUnparsedNotificationDao : UnparsedNotificationDao {
    private val rows = mutableListOf<UnparsedNotificationEntity>()
    private var nextId = 1L

    override suspend fun insert(notification: UnparsedNotificationEntity): Long {
        val withId = notification.copy(id = nextId++)
        rows += withId
        return withId.id
    }

    override fun countByProviderSince(provider: String, since: Long): Flow<Int> =
        MutableStateFlow(rows.count { it.provider == provider && it.occurredAt >= since })

    override suspend fun providersWithUnparsedSince(since: Long): List<String> =
        rows.filter { it.occurredAt >= since }.map { it.provider }.distinct()

    fun all(): List<UnparsedNotificationEntity> = rows.toList()
}

class FakeListenerHeartbeatDao : ListenerHeartbeatDao {
    private val rows = mutableListOf<ListenerHeartbeatEntity>()
    private var nextId = 1L

    override suspend fun insert(heartbeat: ListenerHeartbeatEntity): Long {
        val withId = heartbeat.copy(id = nextId++)
        rows += withId
        return withId.id
    }

    override suspend fun hasDisabledHeartbeatBetween(start: Long, end: Long): Boolean =
        rows.any { !it.wasEnabled && it.timestamp in start..end }

    override suspend fun getLatest(): ListenerHeartbeatEntity? = rows.maxByOrNull { it.timestamp }
}

class FakeTimeProvider(var current: Long = 0L) : com.paybridge.util.TimeProvider {
    override fun nowMillis(): Long = current
}
