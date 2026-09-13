package com.paybridge.domain.model

/** What a PaymentNotificationParser produces from a successfully-read notification. */
data class ParsedPayment(
    val provider: Provider,
    val amount: Long,
    val reference: String? = null,
    val rawText: String,
    val postTime: Long,
)
