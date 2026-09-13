package com.paybridge.data.repository

import com.paybridge.data.local.dao.ListenerHeartbeatDao
import com.paybridge.data.local.entity.ListenerHeartbeatEntity
import com.paybridge.domain.listener.ListenerStatusChecker
import com.paybridge.util.TimeProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Backs the persistent "Listener: ACTIVE / NOT RUNNING" banner. Every call to [checkNow] both
 * updates the in-memory [status] the UI observes AND writes a heartbeat row, so the same check
 * that drives the banner also feeds MatchingEngine's timeout-vs-could-not-verify decision.
 */
class ListenerStatusRepository(
    private val checker: ListenerStatusChecker,
    private val heartbeatDao: ListenerHeartbeatDao,
    private val timeProvider: TimeProvider,
) {
    private val _status = MutableStateFlow(false)
    val status: StateFlow<Boolean> = _status

    suspend fun checkNow(): Boolean {
        val enabled = checker.isEnabled()
        _status.value = enabled
        heartbeatDao.insert(ListenerHeartbeatEntity(timestamp = timeProvider.nowMillis(), wasEnabled = enabled))
        return enabled
    }
}
