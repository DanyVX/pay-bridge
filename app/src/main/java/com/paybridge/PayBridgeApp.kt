package com.paybridge

import android.app.Application
import com.paybridge.data.local.PayBridgeDatabase
import com.paybridge.data.repository.ClaimRepository
import com.paybridge.domain.matching.MatchingEngine
import com.paybridge.parser.ParserRegistry
import com.paybridge.util.SystemTimeProvider
import com.paybridge.util.TimeProvider

/**
 * Composition root. No DI framework (Hilt) is used here — the object graph is small enough
 * that a set of lazily-constructed singletons exposed off this class is easier to read through
 * in a portfolio review than KSP-generated Hilt components, and is a cheap swap later if this
 * ever grows past a handful of injectable classes.
 */
class PayBridgeApp : Application() {

    val database: PayBridgeDatabase by lazy { PayBridgeDatabase.getInstance(this) }

    val timeProvider: TimeProvider by lazy { SystemTimeProvider() }

    private val parserRegistry: ParserRegistry by lazy { ParserRegistry() }

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
}
