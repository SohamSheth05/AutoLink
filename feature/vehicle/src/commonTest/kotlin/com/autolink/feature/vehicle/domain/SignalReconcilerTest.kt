package com.autolink.feature.vehicle.domain

import com.autolink.core.domain.vehicle.SignalQuality
import com.autolink.core.domain.vehicle.SignalSource
import com.autolink.feature.vehicle.domain.SignalReconciler.Decision
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SignalReconcilerTest {
    private val reconciler = SignalReconciler(SignalSource.SIMULATED)
    private fun sample(seq: Long, speed: Float) = VehicleSample(seq, 1_000L * seq, speed, 80, 21f)

    @Test fun empty_snapshot_has_no_values_not_zeros() {
        val s = reconciler.snapshot(0)
        assertEquals(SignalQuality.UNAVAILABLE, s.quality)
        assertNull(s.speedKph)
        assertNull(s.batteryPercent)
    }

    @Test fun newer_sample_is_accepted() {
        assertEquals(Decision.ACCEPTED, reconciler.offer(sample(1, 10f), 0))
        assertEquals(Decision.ACCEPTED, reconciler.offer(sample(2, 20f), 500))
        assertEquals(20f, reconciler.snapshot(500).speedKph)
    }

    @Test fun out_of_order_sample_does_not_regress_state() {
        reconciler.offer(sample(5, 50f), 0)
        assertEquals(Decision.OUT_OF_ORDER, reconciler.offer(sample(3, 0f), 100))
        val s = reconciler.snapshot(100)
        assertEquals(50f, s.speedKph)
        assertEquals(1, s.diagnostics.discardedOutOfOrder)
    }

    @Test fun duplicate_is_counted_and_does_not_refresh_freshness() {
        reconciler.offer(sample(1, 10f), 0)
        assertEquals(Decision.DUPLICATE, reconciler.offer(sample(1, 10f), 1_900))
        val s = reconciler.snapshot(2_000)
        assertEquals(SignalQuality.STALE, s.quality) // the replay at 1.9 s did not count as a fresh signal
        assertEquals(1, s.diagnostics.discardedDuplicate)
        assertEquals(1, s.diagnostics.accepted)
    }
}
