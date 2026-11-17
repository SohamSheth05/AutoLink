package com.autolink.core.common.time

import kotlin.time.Clock
import kotlin.time.TimeSource

/**
 * Monotonic time in milliseconds. Use it for *elapsed-time* questions (is this signal stale?).
 * It never jumps when the user or NTP changes the wall clock. The origin is arbitrary.
 */
fun interface MonotonicClock {
    fun nowMs(): Long
}

/** Wall-clock time (epoch ms). Use it only for display and history, never to measure elapsed time. */
fun interface WallClock {
    fun nowEpochMs(): Long
}

class SystemMonotonicClock : MonotonicClock {
    private val origin = TimeSource.Monotonic.markNow()
    override fun nowMs(): Long = origin.elapsedNow().inWholeMilliseconds
}

class SystemWallClock : WallClock {
    override fun nowEpochMs(): Long = Clock.System.now().toEpochMilliseconds()
}
