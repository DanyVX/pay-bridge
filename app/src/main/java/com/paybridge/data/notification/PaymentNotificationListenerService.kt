package com.paybridge.data.notification

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.paybridge.PayBridgeApp
import com.paybridge.data.local.entity.ListenerHeartbeatEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Reads notifications from the three allowlisted payment apps and hands parseable ones to
 * ClaimRepository. Filtering by [PaymentAppAllowlist] happens first, before any parsing or
 * logging — a notification from any other app is dropped immediately and never touches the
 * rest of the app, per the privacy requirement that PayBridge only ever acts on these three
 * package names.
 *
 * No separate foreground service is used: NotificationListenerService is already a system-bound
 * service, and Android rebinds it after process death on its own (subject to how aggressively
 * the OEM kills background services — exactly the failure mode ListenerHeartbeatEntity exists to
 * detect). onListenerConnected/onListenerDisconnected write heartbeat rows immediately, giving
 * finer-grained signal than the app-resume/WorkManager polling alone.
 */
class PaymentNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val claimRepository by lazy { (application as PayBridgeApp).claimRepository }
    private val listenerHeartbeatDao by lazy { (application as PayBridgeApp).database.listenerHeartbeatDao() }

    override fun onListenerConnected() {
        super.onListenerConnected()
        recordHeartbeat(wasEnabled = true)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        recordHeartbeat(wasEnabled = false)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val provider = PaymentAppAllowlist.providerFor(sbn.packageName) ?: return

        val extras = sbn.notification.extras
        val payload = NotificationPayload(
            packageName = sbn.packageName,
            provider = provider,
            title = extras.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString(),
            text = extras.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString(),
            bigText = extras.getCharSequence(android.app.Notification.EXTRA_BIG_TEXT)?.toString(),
            postTime = sbn.postTime,
            key = sbn.key,
        )

        // Fire-and-forget: onNotificationPosted runs on the main thread and must not block on I/O.
        serviceScope.launch {
            claimRepository.onNotificationReceived(payload)
        }
    }

    private fun recordHeartbeat(wasEnabled: Boolean) {
        serviceScope.launch {
            listenerHeartbeatDao.insert(
                ListenerHeartbeatEntity(timestamp = System.currentTimeMillis(), wasEnabled = wasEnabled)
            )
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
