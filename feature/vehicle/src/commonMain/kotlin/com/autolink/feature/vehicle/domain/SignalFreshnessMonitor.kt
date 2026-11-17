package com.autolink.feature.vehicle.domain

import com.autolink.core.domain.vehicle.SignalQuality

/**
 * Exercise D. Decides LIVE / STALE / UNAVAILABLE from *monotonic* receive times.
 *
 * - No sample yet -> UNAVAILABLE.
 * - Last sample received less than [staleAfterMs] ago -> LIVE.
 * - At or after [staleAfterMs] -> STALE.
 *
 * Receive times that go backwards are ignored, so a late call cannot make the signal look fresher or older.
 * Not thread-safe; callers serialise access.
 */
class SignalFreshnessMonitor(private val staleAfterMs: Long = DEFAULT_STALE_AFTER_MS) {
    init {
        require(staleAfterMs > 0) { "staleAfterMs must be positive" }
    }

    var lastReceivedAtMs: Long? = null
        private set

    fun onSampleReceived(receivedAtMonoMs: Long) {
        val last = lastReceivedAtMs
        if (last == null || receivedAtMonoMs >= last) lastReceivedAtMs = receivedAtMonoMs
    }

    fun quality(nowMonoMs: Long): SignalQuality {
        val last = lastReceivedAtMs ?: return SignalQuality.UNAVAILABLE
        return if (nowMonoMs - last >= staleAfterMs) SignalQuality.STALE else SignalQuality.LIVE
    }

    fun ageMs(nowMonoMs: Long): Long? = lastReceivedAtMs?.let { (nowMonoMs - it).coerceAtLeast(0) }

    companion object {
        const val DEFAULT_STALE_AFTER_MS = 2_000L
    }
}
