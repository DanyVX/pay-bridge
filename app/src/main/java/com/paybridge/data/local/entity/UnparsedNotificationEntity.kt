package com.paybridge.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A notification from an allowlisted payment app that the current parser could not read —
 * because it's an unimplemented stub, a truncated body, or the provider changed its format.
 *
 * This is the single most important long-term reliability signal in the app: without it, a
 * provider silently changing their notification text would break matching with no visible
 * symptom. [UnparsedNotificationWarningTest] and the home/history warning banners are built
 * directly on top of this table.
 */
@Entity(
    tableName = "unparsed_notifications",
    indices = [Index(value = ["provider", "occurredAt"])],
)
data class UnparsedNotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    val packageName: String,

    /** Derived from packageName via the allowlist — known even when the body itself is unreadable. */
    val provider: String,

    val rawTitle: String? = null,
    val rawText: String? = null,

    /** NOT_IMPLEMENTED | TRUNCATED | NO_AMOUNT_FOUND | UNRECOGNIZED_FORMAT */
    val failureReason: String,

    val occurredAt: Long,
)
