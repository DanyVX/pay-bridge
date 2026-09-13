package com.paybridge.data.notification

import com.paybridge.domain.model.Provider

/**
 * Everything extracted from a StatusBarNotification that a parser needs, decoupled from the
 * Android notification APIs so parsers (and their tests) never touch android.service.notification.
 *
 * [text] and [bigText] are both included because payment apps commonly post expanded-style
 * notifications where the meaningful content is in EXTRA_BIG_TEXT rather than EXTRA_TEXT; a
 * parser should prefer whichever is present and treat both being null/blank as truncation, not
 * a crash.
 */
data class NotificationPayload(
    val packageName: String,
    val provider: Provider,
    val title: String?,
    val text: String?,
    val bigText: String?,
    val postTime: Long,
    val key: String,
)
