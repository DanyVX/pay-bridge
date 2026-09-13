package com.paybridge.util

/** Indirection over System.currentTimeMillis() so matching/timeout logic is testable with a fake clock. */
interface TimeProvider {
    fun nowMillis(): Long
}

class SystemTimeProvider : TimeProvider {
    override fun nowMillis(): Long = System.currentTimeMillis()
}
