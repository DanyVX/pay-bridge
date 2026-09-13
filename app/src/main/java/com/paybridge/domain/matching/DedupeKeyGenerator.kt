package com.paybridge.domain.matching

import com.paybridge.domain.model.DedupeKey
import com.paybridge.domain.model.Provider

/**
 * Builds the key used to recognize a redelivered/duplicate notification (common on Xiaomi/Vivo/
 * Oppo after the OS restores a killed notification listener).
 *
 * With a reference: `provider|amount|ref:<reference>` — a reference/transaction ID is
 * essentially unique per real payment, so no time bucketing is needed.
 *
 * Without one: `provider|amount|bucket:<postTime / BUCKET_SIZE_MS>`. This is a deliberate
 * tradeoff: two genuinely distinct payments of the same amount from the same provider within
 * the same bucket would incorrectly collide, but an OS redelivery reuses the exact same
 * postTime and always lands in the same bucket as the original, which is the actual failure
 * mode this exists to catch. BUCKET_SIZE_MS is tunable if real-world use shows it's too coarse
 * or too fine.
 */
object DedupeKeyGenerator {
    private const val BUCKET_SIZE_MS = 60_000L

    fun generate(provider: Provider, amount: Long, reference: String?, postTime: Long): DedupeKey {
        val suffix = if (!reference.isNullOrBlank()) {
            "ref:$reference"
        } else {
            "bucket:${postTime / BUCKET_SIZE_MS}"
        }
        return DedupeKey("$provider|$amount|$suffix")
    }
}
