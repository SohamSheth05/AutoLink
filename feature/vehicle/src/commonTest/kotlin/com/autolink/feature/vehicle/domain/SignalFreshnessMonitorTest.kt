package com.autolink.feature.vehicle.domain

import com.autolink.core.domain.vehicle.SignalQuality.LIVE
import com.autolink.core.domain.vehicle.SignalQuality.STALE
import com.autolink.core.domain.vehicle.SignalQuality.UNAVAILABLE
import kotlin.test.Test
import kotlin.test.assertEquals

class SignalFreshnessMonitorTest {
    private val monitor = SignalFreshnessMonitor(staleAfterMs = 2_000)

    @Test fun unavailable_before_first_sample() = assertEquals(UNAVAILABLE, monitor.quality(0))

    @Test fun live_at_t0_and_just_before_threshold() {
        monitor.onSampleReceived(0)
        assertEquals(LIVE, monitor.quality(0))
        assertEquals(LIVE, monitor.quality(1_999))
    }

    @Test fun stale_at_and_after_threshold() {
        monitor.onSampleReceived(0)
        assertEquals(STALE, monitor.quality(2_000))
        assertEquals(STALE, monitor.quality(10_000))
    }

    @Test fun live_again_after_new_sample() {
        monitor.onSampleReceived(0)
        assertEquals(STALE, monitor.quality(3_000))
        monitor.onSampleReceived(3_000)
        assertEquals(LIVE, monitor.quality(3_100))
    }

    @Test fun earlier_receive_time_is_ignored() {
        monitor.onSampleReceived(5_000)
        monitor.onSampleReceived(1_000)
        assertEquals(5_000, monitor.lastReceivedAtMs)
        assertEquals(LIVE, monitor.quality(6_000))
    }
}
