package com.paybridge.domain.model

/**
 * A claim's lifecycle. Plain enum rather than a sealed class: every state's extra data (matched
 * amount, ambiguity note, etc.) already lives on ClaimEntity regardless of status, so no state
 * here needs a structurally distinct payload.
 */
enum class ClaimStatus {
    /** Waiting for a matching notification; still inside its window. */
    PENDING,

    /** A notification matched this claim's amount (and provider, if specified) exactly. */
    RECEIVED,

    /** A notification arrived in-window for the right provider scope, but a different amount. */
    MISMATCH,

    /** The window elapsed with no match, and the listener was confirmed active throughout. */
    TIMED_OUT,

    /** The window elapsed with no match, but the listener was down for some part of it — an
     *  outcome that must never be shown to the shopkeeper as a plain "not received". */
    COULD_NOT_VERIFY,
}
