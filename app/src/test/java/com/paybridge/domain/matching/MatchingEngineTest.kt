package com.paybridge.domain.matching

import com.paybridge.data.local.entity.ListenerHeartbeatEntity
import com.paybridge.domain.model.ClaimStatus
import com.paybridge.domain.model.Provider
import com.paybridge.testing.FakeClaimDao
import com.paybridge.testing.FakeIncomingNotificationDao
import com.paybridge.testing.FakeListenerHeartbeatDao
import com.paybridge.testing.FakeTimeProvider
import com.paybridge.testing.mockClaim
import com.paybridge.testing.mockIncoming
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MatchingEngineTest {

    private lateinit var claimDao: FakeClaimDao
    private lateinit var incomingDao: FakeIncomingNotificationDao
    private lateinit var heartbeatDao: FakeListenerHeartbeatDao
    private lateinit var timeProvider: FakeTimeProvider
    private lateinit var engine: MatchingEngine

    @Before
    fun setUp() {
        claimDao = FakeClaimDao()
        incomingDao = FakeIncomingNotificationDao()
        heartbeatDao = FakeListenerHeartbeatDao()
        timeProvider = FakeTimeProvider(current = 10_000L)
        engine = MatchingEngine(claimDao, incomingDao, heartbeatDao, timeProvider)
    }

    @Test
    fun `single pending claim, exact amount and provider match, resolves RECEIVED`() = runTest {
        val claimId = claimDao.insert(mockClaim(amount = 500, provider = Provider.NAYAPAY, createdAt = 0, timeoutAt = 60_000))
        val incoming = mockIncoming(amount = 500, provider = Provider.NAYAPAY, receivedAt = 5_000)
        val incomingId = incomingDao.insert(incoming)

        engine.tryMatch(incoming.copy(id = incomingId))

        val claim = claimDao.getById(claimId)!!
        assertEquals(ClaimStatus.RECEIVED.name, claim.status)
        assertEquals(500L, claim.matchedAmount)
        assertFalse(claim.wasAmbiguousMatch)
        assertEquals(claimId, incomingDao.all().single().consumedByClaimId)
    }

    @Test
    fun `provider-less claim is matched by any of the three providers`() = runTest {
        val claimId = claimDao.insert(mockClaim(amount = 750, provider = null, createdAt = 0, timeoutAt = 60_000))
        val incoming = mockIncoming(amount = 750, provider = Provider.JAZZCASH, receivedAt = 1_000)
        val incomingId = incomingDao.insert(incoming)

        engine.tryMatch(incoming.copy(id = incomingId))

        assertEquals(ClaimStatus.RECEIVED.name, claimDao.getById(claimId)!!.status)
    }

    @Test
    fun `provider-set claim is not matched by a different provider's same-amount notification`() = runTest {
        val claimId = claimDao.insert(mockClaim(amount = 750, provider = Provider.NAYAPAY, createdAt = 0, timeoutAt = 60_000))
        val incoming = mockIncoming(amount = 750, provider = Provider.JAZZCASH, receivedAt = 1_000)
        val incomingId = incomingDao.insert(incoming)

        engine.tryMatch(incoming.copy(id = incomingId))

        // Different provider than requested, exact-amount query finds nothing; broadened
        // mismatch query is also provider-scoped so this claim isn't touched either — it
        // stays PENDING until its own timeout.
        assertEquals(ClaimStatus.PENDING.name, claimDao.getById(claimId)!!.status)
        assertNull(incomingDao.all().single().consumedByClaimId)
    }

    @Test
    fun `two pending claims same amount and window - oldest is matched and ambiguity is surfaced`() = runTest {
        val olderId = claimDao.insert(mockClaim(amount = 500, provider = null, createdAt = 1_000, timeoutAt = 60_000))
        val newerId = claimDao.insert(mockClaim(amount = 500, provider = null, createdAt = 2_000, timeoutAt = 60_000))
        val incoming = mockIncoming(amount = 500, provider = Provider.EASYPAISA, receivedAt = 3_000)
        val incomingId = incomingDao.insert(incoming)

        engine.tryMatch(incoming.copy(id = incomingId))

        val older = claimDao.getById(olderId)!!
        val newer = claimDao.getById(newerId)!!
        assertEquals(ClaimStatus.RECEIVED.name, older.status)
        assertEquals(ClaimStatus.PENDING.name, newer.status)
        assertTrue(older.wasAmbiguousMatch)
        assertTrue(older.ambiguityNote!!.contains("2 pending Rs. 500 requests"))
    }

    @Test
    fun `wrong-amount notification with exactly one pending claim in scope resolves MISMATCH`() = runTest {
        val claimId = claimDao.insert(mockClaim(amount = 1500, provider = null, createdAt = 0, timeoutAt = 60_000))
        val incoming = mockIncoming(amount = 1450, provider = Provider.NAYAPAY, receivedAt = 1_000)
        val incomingId = incomingDao.insert(incoming)

        engine.tryMatch(incoming.copy(id = incomingId))

        val claim = claimDao.getById(claimId)!!
        assertEquals(ClaimStatus.MISMATCH.name, claim.status)
        assertEquals(1450L, claim.matchedAmount)
        assertEquals(claimId, incomingDao.all().single().consumedByClaimId)
    }

    @Test
    fun `wrong-amount notification with two candidate claims resolves neither - no guessing`() = runTest {
        val claimAId = claimDao.insert(mockClaim(amount = 1500, provider = null, createdAt = 0, timeoutAt = 60_000))
        val claimBId = claimDao.insert(mockClaim(amount = 2000, provider = null, createdAt = 500, timeoutAt = 60_000))
        val incoming = mockIncoming(amount = 1450, provider = Provider.NAYAPAY, receivedAt = 1_000)
        val incomingId = incomingDao.insert(incoming)

        engine.tryMatch(incoming.copy(id = incomingId))

        assertEquals(ClaimStatus.PENDING.name, claimDao.getById(claimAId)!!.status)
        assertEquals(ClaimStatus.PENDING.name, claimDao.getById(claimBId)!!.status)
        assertNull(incomingDao.all().single().consumedByClaimId)
    }

    @Test
    fun `a claim created after its matching notification already arrived is matched immediately`() = runTest {
        val incoming = mockIncoming(amount = 500, provider = Provider.NAYAPAY, receivedAt = 1_000)
        incomingDao.insert(incoming)
        val claimId = claimDao.insert(mockClaim(amount = 500, provider = Provider.NAYAPAY, createdAt = 500, timeoutAt = 60_000))

        engine.onClaimCreated(claimDao.getById(claimId)!!)

        assertEquals(ClaimStatus.RECEIVED.name, claimDao.getById(claimId)!!.status)
    }

    @Test
    fun `claim past timeout with no listener gap resolves TIMED_OUT`() = runTest {
        val claimId = claimDao.insert(mockClaim(amount = 500, provider = null, createdAt = 0, timeoutAt = 5_000))
        heartbeatDao.insert(ListenerHeartbeatEntity(timestamp = 2_000, wasEnabled = true))

        engine.resolveTimeouts(now = 10_000)

        assertEquals(ClaimStatus.TIMED_OUT.name, claimDao.getById(claimId)!!.status)
    }

    @Test
    fun `claim past timeout with a listener-down heartbeat in its window resolves COULD_NOT_VERIFY`() = runTest {
        val claimId = claimDao.insert(mockClaim(amount = 500, provider = null, createdAt = 0, timeoutAt = 5_000))
        heartbeatDao.insert(ListenerHeartbeatEntity(timestamp = 2_000, wasEnabled = false))

        engine.resolveTimeouts(now = 10_000)

        assertEquals(ClaimStatus.COULD_NOT_VERIFY.name, claimDao.getById(claimId)!!.status)
    }

    @Test
    fun `a still-pending claim before its timeout is left untouched by the sweep`() = runTest {
        val claimId = claimDao.insert(mockClaim(amount = 500, provider = null, createdAt = 0, timeoutAt = 60_000))

        engine.resolveTimeouts(now = 10_000)

        assertEquals(ClaimStatus.PENDING.name, claimDao.getById(claimId)!!.status)
    }
}
