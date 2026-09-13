package com.paybridge.data.notification

import com.paybridge.domain.model.Provider

/**
 * The only packages PayBridge is allowed to act on. Filtering happens against this map before
 * any parsing or logging runs — a notification from a package not listed here is dropped
 * immediately by PaymentNotificationListenerService and never touches the rest of the app. This
 * is a hard privacy requirement, not just an optimization: PayBridge must never read, log, or
 * process notifications from unrelated apps (WhatsApp, SMS replacements, anything else).
 *
 * The package names below are PLACEHOLDERS. Nobody has verified the real package IDs for these
 * three apps against actual installs yet — confirm with `adb shell pm list packages | grep -i
 * <name>` on a device that has them installed before relying on this allowlist.
 */
object PaymentAppAllowlist {
    val PACKAGE_TO_PROVIDER: Map<String, Provider> = mapOf(
        "com.nayapay.nayapay" to Provider.NAYAPAY, // TODO(human): verify real package name
        "pk.com.telenor.easypaisa" to Provider.EASYPAISA, // TODO(human): verify real package name
        "pk.com.jazz.jazzcash" to Provider.JAZZCASH, // TODO(human): verify real package name
    )

    fun providerFor(packageName: String): Provider? = PACKAGE_TO_PROVIDER[packageName]
}
