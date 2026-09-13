package com.paybridge.data.repository

import com.paybridge.data.local.dao.ClaimDao
import com.paybridge.data.local.dao.IncomingNotificationDao
import com.paybridge.data.local.dao.UnparsedNotificationDao
import com.paybridge.data.local.entity.ClaimEntity
import com.paybridge.data.local.entity.IncomingNotificationEntity
import com.paybridge.data.local.entity.UnparsedNotificationEntity
import com.paybridge.data.notification.NotificationPayload
import com.paybridge.domain.matching.DedupeKeyGenerator
import com.paybridge.domain.matching.MatchingEngine
import com.paybridge.domain.model.ClaimStatus
import com.paybridge.domain.model.Provider
import com.paybridge.parser.FailureReason
import com.paybridge.parser.ParseResult
import com.paybridge.parser.ParserRegistry
import com.paybridge.util.TimeProvider
import kotlinx.coroutines.flow.Flow

/**
 * The single entry point between the notification-listener/parser layer and persistence.
 *
 * Ingestion policy lives here rather than in the parsers or the listener service: parsers stay
 * pure (no DB dependency, easy to unit test once real formats exist), and the listener service
 * stays a thin adapter (allowlist check + payload extraction) that can't itself crash on a bad
 * notification.
 */
class ClaimRepository(
    private val claimDao: ClaimDao,
    private val incomingNotificationDao: IncomingNotificationDao,
    private val unparsedNotificationDao: UnparsedNotificationDao,
    private val parserRegistry: ParserRegistry,
    private val timeProvider: TimeProvider,
    private val matchingEngine: MatchingEngine,
) {

    fun observeAllClaims(): Flow<List<ClaimEntity>> = claimDao.observeAll()

    fun observeClaim(id: Long): Flow<ClaimEntity?> = claimDao.observeById(id)

    suspend fun createClaim(amount: Long, provider: Provider?, timeoutMillis: Long): Long {
        val now = timeProvider.nowMillis()
        val claim = ClaimEntity(
            expectedAmount = amount,
            expectedProvider = provider?.name,
            createdAt = now,
            timeoutAt = now + timeoutMillis,
            status = ClaimStatus.PENDING.name,
        )
        val id = claimDao.insert(claim)
        // Catches the case where the payment notification arrived a few seconds before the
        // shopkeeper finished typing the claim.
        matchingEngine.onClaimCreated(claim.copy(id = id))
        return id
    }

    /**
     * Called by PaymentNotificationListenerService for every notification from an allowlisted
     * package. Never throws — a parser's NotImplementedError (today, since none are implemented
     * yet) is caught here and treated exactly like a normal parse Failure, so one bad or
     * unimplemented notification can never take down the listener.
     */
    suspend fun onNotificationReceived(payload: NotificationPayload) {
        val result = try {
            parserRegistry.forProvider(payload.provider).parse(payload)
        } catch (e: NotImplementedError) {
            ParseResult.Failure(FailureReason.NOT_IMPLEMENTED, e.message)
        }

        when (result) {
            is ParseResult.Failure -> logUnparsed(payload, result)
            is ParseResult.Success -> ingestParsed(payload, result)
        }
    }

    private suspend fun logUnparsed(payload: NotificationPayload, failure: ParseResult.Failure) {
        unparsedNotificationDao.insert(
            UnparsedNotificationEntity(
                packageName = payload.packageName,
                provider = payload.provider.name,
                rawTitle = payload.title,
                rawText = payload.bigText ?: payload.text,
                failureReason = failure.reason.name,
                occurredAt = timeProvider.nowMillis(),
            )
        )
    }

    private suspend fun ingestParsed(payload: NotificationPayload, success: ParseResult.Success) {
        val payment = success.payment
        val dedupeKey = DedupeKeyGenerator.generate(
            provider = payment.provider,
            amount = payment.amount,
            reference = payment.reference,
            postTime = payment.postTime,
        )

        // A one-minute lookup window either side is enough to catch a same-bucket redelivery
        // without scanning the whole table; the bucket itself is what actually enforces dedup.
        val bucketWindowMs = 60_000L
        val alreadySeen = incomingNotificationDao.existsWithDedupeKeyWithinBucket(
            key = dedupeKey.value,
            bucketStart = payment.postTime - bucketWindowMs,
            bucketEnd = payment.postTime + bucketWindowMs,
        )
        if (alreadySeen) {
            // Expected OEM redelivery behavior, not a parse failure — dropped silently, and
            // deliberately never counted toward the unparsed-notification warning.
            return
        }

        val entity = IncomingNotificationEntity(
            provider = payment.provider.name,
            amount = payment.amount,
            reference = payment.reference,
            rawText = payment.rawText,
            postTime = payment.postTime,
            receivedAt = timeProvider.nowMillis(),
            dedupeKey = dedupeKey.value,
        )
        val id = incomingNotificationDao.insert(entity)
        matchingEngine.tryMatch(entity.copy(id = id))
    }
}
