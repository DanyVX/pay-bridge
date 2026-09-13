package com.paybridge.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A shopkeeper's "expecting Rs. X" claim, from creation through its terminal state.
 *
 * Deliberately one row per claim rather than a separate "matched transaction" table: every
 * edge case this app cares about (mismatch, ambiguity, timeout vs. could-not-verify) is framed
 * as "what happened to claim N", not "enumerate all money that ever arrived". Money that
 * arrives with no open claim is tracked in [IncomingNotificationEntity] instead, and is never
 * surfaced to the shopkeeper as its own concept in v1.
 *
 * [status] holds a [com.paybridge.domain.model.ClaimStatus] name as a plain string rather than
 * via a Room TypeConverter — there's exactly one enum column and a converter would be pure
 * ceremony for it.
 */
@Entity(
    tableName = "claims",
    indices = [Index(value = ["status"]), Index(value = ["createdAt"])],
)
data class ClaimEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    /** Whole-rupee amount the shopkeeper is expecting. Always matched exactly, never rounded. */
    val expectedAmount: Long,

    /** Provider name (see Provider enum), or null for a provider-less "match any" claim. */
    val expectedProvider: String?,

    /** Epoch millis when the claim was created — also the start of its matching window. */
    val createdAt: Long,

    /** Epoch millis after which an unresolved claim should time out. */
    val timeoutAt: Long,

    /** ClaimStatus name: PENDING / RECEIVED / MISMATCH / TIMED_OUT / COULD_NOT_VERIFY. */
    val status: String,

    /** Epoch millis when the claim left PENDING, or null while still pending. */
    val resolvedAt: Long? = null,

    // Populated only once resolved as RECEIVED or MISMATCH — the shopkeeper's audit trail.
    val matchedNotificationId: Long? = null,
    val matchedAmount: Long? = null,
    val matchedProvider: String? = null,
    val matchedRawText: String? = null,

    /** True if 2+ pending claims tied on amount/provider/window when this one was matched. */
    val wasAmbiguousMatch: Boolean = false,

    /** Human-readable note shown alongside an ambiguous match, e.g. "2 pending Rs. 500 requests — matched the older one at 14:32". */
    val ambiguityNote: String? = null,
)
