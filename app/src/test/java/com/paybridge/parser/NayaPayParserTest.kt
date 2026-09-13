package com.paybridge.parser

import com.paybridge.data.notification.NotificationPayload
import com.paybridge.domain.model.Provider
import org.junit.Assert.assertThrows
import org.junit.Ignore
import org.junit.Test

class NayaPayParserTest {

    private val parser = NayaPayParser()

    @Test
    fun `parse throws NotImplementedError until real notification format is supplied`() {
        val payload = NotificationPayload(
            packageName = "com.nayapay.nayapay",
            provider = Provider.NAYAPAY,
            title = "placeholder — not a real NayaPay notification",
            text = "placeholder — not a real NayaPay notification",
            bigText = null,
            postTime = 0L,
            key = "test-key",
        )
        assertThrows(NotImplementedError::class.java) { parser.parse(payload) }
    }

    @Test
    @Ignore("Real NayaPay notification format not yet documented — see /docs/notification-samples.md")
    fun `parse extracts amount from a real received-payment notification`() {
        // Once /docs/notification-samples.md has a real Sample 1 for NayaPay, replace this with
        // a NotificationPayload built from that sample and assert the correct ParsedPayment.
    }

    @Test
    @Ignore("Real NayaPay notification format not yet documented — see /docs/notification-samples.md")
    fun `parse extracts a different amount correctly`() {
        // Covers /docs/notification-samples.md Sample 2 (a different amount than Sample 1).
    }

    @Test
    @Ignore("Real NayaPay notification format not yet documented — see /docs/notification-samples.md")
    fun `parse returns Failure TRUNCATED when text and bigText are both blank`() {
    }

    @Test
    @Ignore("Real NayaPay notification format not yet documented — see /docs/notification-samples.md")
    fun `parse returns Failure UNRECOGNIZED_FORMAT for text that does not match the known shape`() {
    }
}
