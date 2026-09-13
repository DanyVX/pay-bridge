package com.paybridge.domain.matching

import com.paybridge.data.local.dao.ClaimDao
import com.paybridge.data.local.dao.IncomingNotificationDao
import com.paybridge.data.local.dao.ListenerHeartbeatDao
import com.paybridge.data.local.entity.ClaimEntity
import com.paybridge.data.local.entity.IncomingNotificationEntity
import com.paybridge.domain.model.ClaimStatus
import com.paybridge.util.TimeProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pure matching/timeout logic over the Room DAOs — no Android framework dependency, so it's
 * fully unit-testable with fakes and a fake clock (see MatchingEngineTest).
 *
 * Every rule here maps directly to an edge case from the project brief:
 *  - amount matching is always exact, never fuzzy/rounded (see [tryExactMatch])
 *  - two same-amount pending claims: the OLDEST is matched, ambiguity is surfaced not hidden
 *  - a wrong-amount notification only resolves a claim as MISMATCH when exactly one pending
 *    claim is in scope — with two or more, we don't guess which one it was meant for
 *  - a claim that times out while the listener was down anywhere in its window resolves to
 *    COULD_NOT_VERIFY, never a plain TIMED_OUT — those mean very different things to a
 *    shopkeeper deciding whether to hand over goods
 */
class MatchingEngine(
    private val claimDao: ClaimDao,
    private val incomingNotificationDao: IncomingNotificationDao,
    private val listenerHeartbeatDao: ListenerHeartbeatDao,
    private val timeProvider: TimeProvider,
) {

    /** Called by ClaimRepository right after a new notification is logged. */
    suspend fun tryMatch(incoming: IncomingNotificationEntity) {
        val exactCandidates = claimDao.findExactMatchCandidates(
            amount = incoming.amount,
            provider = incoming.provider,
            receivedAt = incoming.receivedAt,
        )
        if (exactCandidates.isNotEmpty()) {
            resolveExactMatch(incoming, exactCandidates)
            return
        }
        tryMismatch(incoming)
    }

    /** Called right after a claim is created, to catch a notification that arrived moments earlier. */
    suspend fun onClaimCreated(claim: ClaimEntity) {
        val candidates = incomingNotificationDao.findUnconsumedMatching(
            amount = claim.expectedAmount,
            claimProvider = claim.expectedProvider,
            windowStart = claim.createdAt,
            windowEnd = claim.timeoutAt,
        )
        val chosen = candidates.firstOrNull() ?: return
        markReceived(claim, chosen, ambiguous = false, ambiguityNote = null)
        incomingNotificationDao.markConsumed(chosen.id, claim.id)
    }

    /** Periodic sweep (WorkManager backstop + UI-driven ticker) resolving claims past their window. */
    suspend fun resolveTimeouts(now: Long = timeProvider.nowMillis()) {
        val overdue = claimDao.getPendingPastTimeout(now)
        for (claim in overdue) {
            val listenerWasDown = listenerHeartbeatDao.hasDisabledHeartbeatBetween(claim.createdAt, claim.timeoutAt)
            val status = if (listenerWasDown) ClaimStatus.COULD_NOT_VERIFY else ClaimStatus.TIMED_OUT
            claimDao.update(claim.copy(status = status.name, resolvedAt = now))
        }
    }

    private suspend fun resolveExactMatch(incoming: IncomingNotificationEntity, candidates: List<ClaimEntity>) {
        // findExactMatchCandidates already orders oldest-first, so the head is the correct pick.
        val oldest = candidates.first()
        val ambiguous = candidates.size > 1
        val note = if (ambiguous) {
            "${candidates.size} pending Rs. ${incoming.amount} requests — matched the older one at ${formatTime(oldest.createdAt)}"
        } else null

        markReceived(oldest, incoming, ambiguous, note)
        incomingNotificationDao.markConsumed(incoming.id, oldest.id)
    }

    private suspend fun tryMismatch(incoming: IncomingNotificationEntity) {
        val scoped = claimDao.findProviderScopedPending(incoming.provider, incoming.receivedAt)
        // Exactly one candidate: safe enough to assume this notification was meant for it. Two
        // or more: we genuinely can't attribute a wrong-amount notification, so we leave it
        // unconsumed and let each claim reach its own timeout independently rather than guess.
        if (scoped.size != 1) return

        val claim = scoped.single()
        claimDao.update(
            claim.copy(
                status = ClaimStatus.MISMATCH.name,
                resolvedAt = timeProvider.nowMillis(),
                matchedNotificationId = incoming.id,
                matchedAmount = incoming.amount,
                matchedProvider = incoming.provider,
                matchedRawText = incoming.rawText,
            )
        )
        incomingNotificationDao.markConsumed(incoming.id, claim.id)
    }

    private suspend fun markReceived(
        claim: ClaimEntity,
        incoming: IncomingNotificationEntity,
        ambiguous: Boolean,
        ambiguityNote: String?,
    ) {
        claimDao.update(
            claim.copy(
                status = ClaimStatus.RECEIVED.name,
                resolvedAt = timeProvider.nowMillis(),
                matchedNotificationId = incoming.id,
                matchedAmount = incoming.amount,
                matchedProvider = incoming.provider,
                matchedRawText = incoming.rawText,
                wasAmbiguousMatch = ambiguous,
                ambiguityNote = ambiguityNote,
            )
        )
    }

    private fun formatTime(epochMillis: Long): String =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(epochMillis))
}
