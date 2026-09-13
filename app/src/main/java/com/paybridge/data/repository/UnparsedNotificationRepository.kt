package com.paybridge.data.repository

import com.paybridge.data.local.dao.UnparsedNotificationDao
import com.paybridge.domain.model.Provider
import com.paybridge.util.TimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.concurrent.TimeUnit

/**
 * Backs the "N unparsed notifications from X in the last 24h — matching may be broken" warning
 * banner. This is the single most important long-term reliability signal in the app: without
 * it, a provider changing their notification format would silently break matching.
 */
class UnparsedNotificationRepository(
    private val dao: UnparsedNotificationDao,
    private val timeProvider: TimeProvider,
) {
    private val windowMs = TimeUnit.HOURS.toMillis(24)

    /** Provider -> count of unparsed notifications in the last 24h, omitting zero-count providers. */
    fun countsLast24h(): Flow<Map<Provider, Int>> {
        val since = timeProvider.nowMillis() - windowMs
        val perProvider = Provider.entries.map { provider ->
            dao.countByProviderSince(provider.name, since)
        }
        return combine(perProvider) { counts ->
            Provider.entries.zip(counts.toList())
                .filter { (_, count) -> count > 0 }
                .toMap()
        }
    }
}
