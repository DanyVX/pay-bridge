package com.paybridge.data.repository

import com.paybridge.data.notification.NotificationPayload
import com.paybridge.domain.matching.MatchingEngine
import com.paybridge.domain.model.Provider
import com.paybridge.parser.ParserRegistry
import com.paybridge.parser.PaymentNotificationParser
import com.paybridge.parser.ParseResult
import com.paybridge.testing.FakeClaimDao
import com.paybridge.testing.FakeIncomingNotificationDao
import com.paybridge.testing.FakeListenerHeartbeatDao
import com.paybridge.testing.FakeTimeProvider
import com.paybridge.testing.FakeUnparsedNotificationDao
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * MOCK DATA NOTICE: every NotificationPayload built in this file is a synthetic test fixture
 * for exercising the ingestion pipeline's control flow (parse -> dedupe -> log). None of it is
 * real NayaPay/Easypaisa/JazzCash notification text — see /docs/notification-samples.md.
 */
class ClaimRepositoryTest {

    private lateinit var claimDao: FakeClaimDao
    private lateinit var incomingDao: FakeIncomingNotificationDao
    private lateinit var unparsedDao: FakeUnparsedNotificationDao
    private lateinit var timeProvider: FakeTimeProvider

    private fun repositoryWith(parser: PaymentNotificationParser): ClaimRepository {
        claimDao = FakeClaimDao()
        incomingDao = FakeIncomingNotificationDao()
        unparsedDao = FakeUnparsedNotificationDao()
        timeProvider = FakeTimeProvider(1_000_000L)
        val matchingEngine = MatchingEngine(
            claimDao = claimDao,
            incomingNotificationDao = incomingDao,
            listenerHeartbeatDao = FakeListenerHeartbeatDao(),
            timeProvider = timeProvider,
        )
        return ClaimRepository(
            claimDao = claimDao,
            incomingNotificationDao = incomingDao,
            unparsedNotificationDao = unparsedDao,
            parserRegistry = ParserRegistry(mapOf(Provider.NAYAPAY to parser)),
            timeProvider = timeProvider,
            matchingEngine = matchingEngine,
        )
    }

    private fun mockPayload(text: String = "MOCK — not real provider text") = NotificationPayload(
        packageName = "mock.nayapay.package",
        provider = Provider.NAYAPAY,
        title = "MOCK payment received",
        text = text,
        bigText = null,
        postTime = 1_000_000L,
        key = "mock-key-1",
    )

    @Test
    fun `a stub parser's NotImplementedError is logged as an unparsed notification, not thrown`() = runTest {
        val stubParser = object : PaymentNotificationParser {
            override val provider = Provider.NAYAPAY
            override fun parse(payload: NotificationPayload): ParseResult =
                throw NotImplementedError("stub")
        }
        val repository = repositoryWith(stubParser)

        repository.onNotificationReceived(mockPayload())

        assertEquals(1, unparsedDao.all().size)
        assertEquals("NOT_IMPLEMENTED", unparsedDao.all().single().failureReason)
        assertTrue(incomingDao.all().isEmpty())
    }

    @Test
    fun `a parse Failure is logged with its own reason`() = runTest {
        val failingParser = object : PaymentNotificationParser {
            override val provider = Provider.NAYAPAY
            override fun parse(payload: NotificationPayload): ParseResult =
                ParseResult.Failure(com.paybridge.parser.FailureReason.TRUNCATED)
        }
        val repository = repositoryWith(failingParser)

        repository.onNotificationReceived(mockPayload())

        assertEquals("TRUNCATED", unparsedDao.all().single().failureReason)
    }

    @Test
    fun `a successful parse is logged as an incoming notification`() = runTest {
        val successParser = object : PaymentNotificationParser {
            override val provider = Provider.NAYAPAY
            override fun parse(payload: NotificationPayload): ParseResult =
                ParseResult.Success(
                    com.paybridge.domain.model.ParsedPayment(
                        provider = Provider.NAYAPAY,
                        amount = 500,
                        rawText = "MOCK — Rs. 500 received",
                        postTime = payload.postTime,
                    )
                )
        }
        val repository = repositoryWith(successParser)

        repository.onNotificationReceived(mockPayload())

        assertEquals(1, incomingDao.all().size)
        assertEquals(500L, incomingDao.all().single().amount)
        assertTrue(unparsedDao.all().isEmpty())
    }

    @Test
    fun `a duplicate notification within the same dedupe bucket is dropped, not double-logged`() = runTest {
        val successParser = object : PaymentNotificationParser {
            override val provider = Provider.NAYAPAY
            override fun parse(payload: NotificationPayload): ParseResult =
                ParseResult.Success(
                    com.paybridge.domain.model.ParsedPayment(
                        provider = Provider.NAYAPAY,
                        amount = 500,
                        rawText = "MOCK — Rs. 500 received",
                        postTime = payload.postTime,
                    )
                )
        }
        val repository = repositoryWith(successParser)

        // Simulates an OEM redelivering the exact same notification after restoring the
        // listener from a killed state — same postTime both times.
        repository.onNotificationReceived(mockPayload())
        repository.onNotificationReceived(mockPayload())

        assertEquals(1, incomingDao.all().size)
        assertTrue(unparsedDao.all().isEmpty())
    }
}
