package com.paybridge.parser

import com.paybridge.data.notification.NotificationPayload
import com.paybridge.domain.model.Provider
import org.junit.Assert.assertThrows
import org.junit.Ignore
import org.junit.Test

class JazzCashParserTest {

    private val parser = JazzCashParser()

    @Test
    fun `parse throws NotImplementedError until real notification format is supplied`() {
        val payload = NotificationPayload(
            packageName = "placeholder.jazzcash.package",
            provider = Provider.JAZZCASH,
            title = "placeholder — not a real JazzCash notification",
            text = "placeholder — not a real JazzCash notification",
            bigText = null,
            postTime = 0L,
            key = "test-key",
        )
        assertThrows(NotImplementedError::class.java) { parser.parse(payload) }
    }

    @Test
    @Ignore("Real JazzCash notification format not yet documented — see /docs/notification-samples.md")
    fun `parse extracts amount from a real received-payment notification`() {
    }

    @Test
    @Ignore("Real JazzCash notification format not yet documented — see /docs/notification-samples.md")
    fun `parse extracts a different amount correctly`() {
    }

    @Test
    @Ignore("Real JazzCash notification format not yet documented — see /docs/notification-samples.md")
    fun `parse returns Failure TRUNCATED when text and bigText are both blank`() {
    }

    @Test
    @Ignore("Real JazzCash notification format not yet documented — see /docs/notification-samples.md")
    fun `parse returns Failure UNRECOGNIZED_FORMAT for text that does not match the known shape`() {
    }
}
