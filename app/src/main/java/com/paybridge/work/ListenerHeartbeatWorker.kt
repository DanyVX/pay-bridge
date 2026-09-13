package com.paybridge.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.paybridge.PayBridgeApp

/**
 * Closes a real gap in listener-down detection: app-resume checks alone can't tell whether the
 * listener died while the app was closed the whole time a claim was open, which would otherwise
 * mislabel that claim TIMED_OUT instead of COULD_NOT_VERIFY. WorkManager's periodic floor is 15
 * minutes — coarser than the 5-10 minute default claim timeout, so this is a backstop that
 * narrows the blind spot rather than closing it completely, on top of the resume-time and
 * listener-connect/disconnect checks that cover the common case.
 */
class ListenerHeartbeatWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as PayBridgeApp
        app.listenerStatusRepository.checkNow()
        return Result.success()
    }
}
