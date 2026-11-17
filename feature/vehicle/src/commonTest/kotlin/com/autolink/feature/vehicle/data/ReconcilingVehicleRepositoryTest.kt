package com.autolink.feature.vehicle.data

import app.cash.turbine.test
import com.autolink.core.domain.vehicle.SignalQuality
import com.autolink.feature.vehicle.FakeVehicleSignalSource
import com.autolink.feature.vehicle.data.repository.ReconcilingVehicleRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Runs on virtual time: the monotonic clock *is* the test scheduler, so "wait 2 s" takes no real time. */
@OptIn(ExperimentalCoroutinesApi::class)
class ReconcilingVehicleRepositoryTest {

    @Test
    fun goes_unavailable_then_live_then_stale_then_live() = runTest {
        val source = FakeVehicleSignalSource()
        val repo = ReconcilingVehicleRepository(source, clock = { testScheduler.currentTime })

        repo.observeStatus().map { it.quality }.distinctUntilChanged().test {
            assertEquals(SignalQuality.UNAVAILABLE, awaitItem())
            runCurrent()

            source.emit(1, 30f)
            assertEquals(SignalQuality.LIVE, awaitItem())

            // No samples for 2 s: the freshness tick, not an event, must flip the state.
            advanceTimeBy(2_001)
            assertEquals(SignalQuality.STALE, awaitItem())

            source.emit(2, 31f)
            assertEquals(SignalQuality.LIVE, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun out_of_order_sample_never_reaches_the_ui() = runTest {
        val source = FakeVehicleSignalSource()
        val repo = ReconcilingVehicleRepository(source, clock = { testScheduler.currentTime })

        repo.observeStatus().test {
            awaitItem() // unavailable
            runCurrent()
            source.emit(5, 50f)
            assertEquals(50f, awaitItem().speedKph)
            source.emit(3, 0f)
            val afterOld = awaitItem()
            assertEquals(50f, afterOld.speedKph)
            assertEquals(1, afterOld.diagnostics.discardedOutOfOrder)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun only_accepted_samples_reach_the_sink() = runTest {
        val source = FakeVehicleSignalSource()
        val recorded = mutableListOf<Long>()
        val repo = ReconcilingVehicleRepository(
            source,
            clock = { testScheduler.currentTime },
            sink = { recorded += it.seq },
        )

        repo.observeStatus().test {
            awaitItem()
            runCurrent()
            source.emit(1, 10f)
            source.emit(1, 10f) // duplicate
            source.emit(3, 30f)
            source.emit(2, 20f) // out of order
            runCurrent()
            assertEquals(listOf(1L, 3L), recorded)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
