package com.paybridge.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.paybridge.PayBridgeApp

/**
 * Coarse background backstop for resolving timed-out claims while the app isn't open. The
 * primary, timely resolution path is a Compose LaunchedEffect ticking every ~10-30s while a
 * claim/history screen is visible — WorkManager's 15-minute periodic floor is too coarse to be
 * the main mechanism for a 5-10 minute claim timeout, but it guarantees a claim doesn't stay
 * PENDING forever just because the app was closed the whole time.
 */
class ClaimTimeoutWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as PayBridgeApp
        app.matchingEngine.resolveTimeouts()
        return Result.success()
    }
}
