package com.paybridge.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Every notification from an allowlisted payment app that the parser layer successfully read,
 * logged *before* matching is attempted.
 *
 * This table exists mainly so dedupe state and "arrived before the claim was created" matching
 * both survive process death — neither can safely live only in the matching engine's memory,
 * since the whole point of this app is to keep working across OS-driven kills.
 */
@Entity(
    tableName = "incoming_notifications",
    indices = [
        Index(value = ["dedupeKey"]),
        Index(value = ["provider", "amount", "consumedByClaimId"]),
    ],
)
data class IncomingNotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    /** Provider enum name — always concrete; unlike a claim, a real notification always has one. */
    val provider: String,

    val amount: Long,

    /** Parser-extracted transaction reference, if the notification format includes one. */
    val reference: String? = null,

    /** Full notification text — kept long-term as the shopkeeper's audit trail (per product decision). */
    val rawText: String,

    /** StatusBarNotification post time, as reported by the OS. */
    val postTime: Long,

    /** When PayBridge actually processed it — can lag postTime under OEM delivery delay. */
    val receivedAt: Long,

    /**
     * provider|amount|ref:<reference> when a reference is available, otherwise
     * provider|amount|bucket:<postTime / 60_000> — see DedupeKeyGenerator for the full rationale
     * and the known collision tradeoff of the bucketed fallback.
     */
    val dedupeKey: String,

    /** Set once this notification resolves a claim; null while still available to match against. */
    val consumedByClaimId: Long? = null,
)
