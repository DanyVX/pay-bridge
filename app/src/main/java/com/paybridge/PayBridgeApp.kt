package com.paybridge

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.paybridge.data.local.OnboardingPreferences
import com.paybridge.data.local.PayBridgeDatabase
import com.paybridge.data.repository.ClaimRepository
import com.paybridge.data.repository.ListenerStatusRepository
import com.paybridge.domain.listener.ListenerStatusChecker
import com.paybridge.domain.matching.MatchingEngine
import com.paybridge.parser.ParserRegistry
import com.paybridge.util.SystemTimeProvider
import com.paybridge.util.TimeProvider
import com.paybridge.work.ClaimTimeoutWorker
import com.paybridge.work.ListenerHeartbeatWorker
import java.util.concurrent.TimeUnit

/**
 * Composition root. No DI framework (Hilt) is used here — the object graph is small enough
 * that a set of lazily-constructed singletons exposed off this class is easier to read through
 * in a portfolio review than KSP-generated Hilt components, and is a cheap swap later if this
 * ever grows past a handful of injectable classes.
 */
class PayBridgeApp : Application() {

    val database: PayBridgeDatabase by lazy { PayBridgeDatabase.getInstance(this) }

    val onboardingPreferences: OnboardingPreferences by lazy { OnboardingPreferences(this) }

    val timeProvider: TimeProvider by lazy { SystemTimeProvider() }

    private val parserRegistry: ParserRegistry by lazy { ParserRegistry() }

    private val listenerStatusChecker: ListenerStatusChecker by lazy { ListenerStatusChecker(this) }

    val listenerStatusRepository: ListenerStatusRepository by lazy {
        ListenerStatusRepository(
            checker = listenerStatusChecker,
            heartbeatDao = database.listenerHeartbeatDao(),
            timeProvider = timeProvider,
        )
    }

    val matchingEngine: MatchingEngine by lazy {
        MatchingEngine(
            claimDao = database.claimDao(),
            incomingNotificationDao = database.incomingNotificationDao(),
            listenerHeartbeatDao = database.listenerHeartbeatDao(),
            timeProvider = timeProvider,
        )
    }

    val claimRepository: ClaimRepository by lazy {
        ClaimRepository(
            claimDao = database.claimDao(),
            incomingNotificationDao = database.incomingNotificationDao(),
            unparsedNotificationDao = database.unparsedNotificationDao(),
            parserRegistry = parserRegistry,
            timeProvider = timeProvider,
            matchingEngine = matchingEngine,
        )
    }

    override fun onCreate() {
        super.onCreate()
        schedulePeriodicWork()
    }

    private fun schedulePeriodicWork() {
        val workManager = WorkManager.getInstance(this)

        // 15 minutes is WorkManager's periodic floor — see ListenerHeartbeatWorker/ClaimTimeoutWorker
        // doc comments for why each is a backstop rather than the primary mechanism.
        workManager.enqueueUniquePeriodicWork(
            "listener-heartbeat",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<ListenerHeartbeatWorker>(15, TimeUnit.MINUTES).build(),
        )
        workManager.enqueueUniquePeriodicWork(
            "claim-timeout-sweep",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<ClaimTimeoutWorker>(15, TimeUnit.MINUTES).build(),
        )
    }
}
