package com.paybridge.parser

import com.paybridge.data.notification.NotificationPayload
import com.paybridge.domain.model.Provider

/**
 * STUB. Nobody on this project has captured a real NayaPay payment-received notification yet,
 * so this deliberately does not guess at a format — see the project brief's explicit warning
 * against inventing a "plausible looking" regex here.
 */
class NayaPayParser : PaymentNotificationParser {
    override val provider = Provider.NAYAPAY

    override fun parse(payload: NotificationPayload): ParseResult {
        // TODO(human): fill in with real notification format — see /docs/notification-samples.md
        throw NotImplementedError(
            "NayaPayParser.parse() is unimplemented — capture real samples in " +
                "/docs/notification-samples.md before writing this parser."
        )
    }
}
