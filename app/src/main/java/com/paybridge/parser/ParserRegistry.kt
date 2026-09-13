package com.paybridge.parser

import com.paybridge.domain.model.Provider

/** Provider -> parser lookup. Constructed once and reused (see PayBridgeApp). */
class ParserRegistry(
    private val parsers: Map<Provider, PaymentNotificationParser> = mapOf(
        Provider.NAYAPAY to NayaPayParser(),
        Provider.EASYPAISA to EasypaisaParser(),
        Provider.JAZZCASH to JazzCashParser(),
    ),
) {
    fun forProvider(provider: Provider): PaymentNotificationParser =
        parsers.getValue(provider)
}
