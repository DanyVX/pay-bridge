package com.paybridge.parser

import com.paybridge.data.notification.NotificationPayload
import com.paybridge.domain.model.ParsedPayment
import com.paybridge.domain.model.Provider

/** Why a parser couldn't produce a ParsedPayment from a given notification. */
enum class FailureReason {
    /** The parser is a stub — see the TODO(human) in the concrete implementation. */
    NOT_IMPLEMENTED,

    /** Both text and bigText were missing/blank, or clearly cut short by the OS. */
    TRUNCATED,

    /** The text was readable but no amount could be extracted from it. */
    NO_AMOUNT_FOUND,

    /** The text didn't match any known shape for this provider — may mean the format changed. */
    UNRECOGNIZED_FORMAT,
}

sealed class ParseResult {
    data class Success(val payment: ParsedPayment) : ParseResult()
    data class Failure(val reason: FailureReason, val detail: String? = null) : ParseResult()
}

/**
 * One implementation per provider (NayaPayParser, EasypaisaParser, JazzCashParser).
 *
 * Contract for a *finished* implementation: [parse] must never throw. A truncated or
 * unexpected notification is a [ParseResult.Failure], not an exception — the whole matching
 * pipeline depends on a single bad notification never being able to crash the listener service.
 *
 * The three concrete implementations in this codebase are honest stubs that throw
 * NotImplementedError instead, because nobody has yet captured the real notification text
 * format for any of these three apps. ClaimRepository catches that exception at the boundary
 * and treats it identically to Failure(NOT_IMPLEMENTED) — see /docs/notification-samples.md for
 * how to fill these in for real.
 */
interface PaymentNotificationParser {
    val provider: Provider
    fun parse(payload: NotificationPayload): ParseResult
}
