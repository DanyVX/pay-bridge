package com.paybridge.testing

// MOCK DATA — these are synthetic test fixtures for exercising MatchingEngine's control flow.
// None of this is real NayaPay/Easypaisa/JazzCash notification text; see
// /docs/notification-samples.md for how real samples will eventually be captured.

import com.paybridge.data.local.entity.ClaimEntity
import com.paybridge.data.local.entity.IncomingNotificationEntity
import com.paybridge.domain.model.ClaimStatus
import com.paybridge.domain.model.Provider

fun mockClaim(
    amount: Long,
    provider: Provider? = null,
    createdAt: Long,
    timeoutAt: Long,
    status: ClaimStatus = ClaimStatus.PENDING,
) = ClaimEntity(
    expectedAmount = amount,
    expectedProvider = provider?.name,
    createdAt = createdAt,
    timeoutAt = timeoutAt,
    status = status.name,
)

fun mockIncoming(
    amount: Long,
    provider: Provider,
    receivedAt: Long,
    reference: String? = null,
    rawText: String = "MOCK — Rs. $amount received via $provider (not real provider text)",
) = IncomingNotificationEntity(
    provider = provider.name,
    amount = amount,
    reference = reference,
    rawText = rawText,
    postTime = receivedAt,
    receivedAt = receivedAt,
    dedupeKey = "mock|$provider|$amount|$receivedAt",
)
