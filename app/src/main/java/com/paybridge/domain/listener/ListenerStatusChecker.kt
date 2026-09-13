package com.paybridge.domain.listener

import android.content.Context
import androidx.core.app.NotificationManagerCompat

/**
 * Wraps the one API Android actually offers for asking "is our notification listener currently
 * enabled" — there is no push callback for this, only a poll-able snapshot, which is why this
 * is called from several places (app resume, the listener's own connect/disconnect callbacks,
 * and a periodic background worker) rather than checked once at permission-grant time.
 */
class ListenerStatusChecker(private val context: Context) {

    fun isEnabled(): Boolean {
        val enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(context)
        return context.packageName in enabledPackages
    }
}
