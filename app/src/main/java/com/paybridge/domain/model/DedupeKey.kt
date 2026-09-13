package com.paybridge.domain.model

/**
 * A typed wrapper around the raw dedupe string so callers can't accidentally pass a random
 * String where a dedupe key is expected. See DedupeKeyGenerator for how the value is built.
 */
@JvmInline
value class DedupeKey(val value: String)
