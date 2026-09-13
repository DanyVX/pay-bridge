package com.paybridge.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A point-in-time record of whether Android reported PayBridge's notification listener as
 * enabled. Android gives no push callback for "your listener just got killed" — only a
 * poll-able enabled/disabled state — so this table is a log of checks, not a continuous signal.
 *
 * Rows are written from three places: every app resume, the listener's own
 * onListenerConnected/onListenerDisconnected callbacks (finer-grained than polling), and a
 * periodic WorkManager job (ListenerHeartbeatWorker) that keeps checking even while the app is
 * closed — otherwise a claim opened and timed out entirely while the app is backgrounded could
 * be mislabeled TIMED_OUT when the listener was actually down the whole time.
 *
 * At timeout-resolution time, the matching engine asks "was there any wasEnabled=false row
 * between this claim's createdAt and timeoutAt?" — if yes, the claim resolves to
 * COULD_NOT_VERIFY instead of TIMED_OUT, since those mean very different things to a shopkeeper
 * deciding whether to hand over goods.
 */
@Entity(
    tableName = "listener_heartbeats",
    indices = [Index(value = ["timestamp"])],
)
data class ListenerHeartbeatEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val wasEnabled: Boolean,
)
