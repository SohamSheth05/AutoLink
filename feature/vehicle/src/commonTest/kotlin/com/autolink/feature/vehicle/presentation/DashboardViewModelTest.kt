package com.autolink.feature.vehicle.presentation

import app.cash.turbine.test
import com.autolink.core.common.time.WallClock
import com.autolink.core.domain.vehicle.SignalQuality
import com.autolink.feature.vehicle.FakeTelemetryQueue
import com.autolink.feature.vehicle.data.telemetry.TelemetryRecorder
import com.autolink.feature.vehicle.data.repository.ReconcilingVehicleRepository
import com.autolink.feature.vehicle.data.simulator.SimulatedVehicleSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** End-to-end through the real simulator and repository, on virtual time. */
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun pausing_the_simulator_makes_the_dashboard_stale_and_resume_recovers() = runTest(dispatcher) {
        val sim = SimulatedVehicleSource(wallClock = WallClock { testScheduler.currentTime })
        val queue = FakeTelemetryQueue()
        val repo = ReconcilingVehicleRepository(
            sim,
            clock = { testScheduler.currentTime },
            sink = TelemetryRecorder(queue, vehicleId = "demo-vehicle-001"),
        )
        val vm = DashboardViewModel(repo, queue, sim)

        vm.state.test {
            advanceTimeBy(1_100)
            assertEquals(SignalQuality.LIVE, expectMostRecentItem().status.quality)

            vm.onAction(DashboardAction.OnTogglePause)
            advanceTimeBy(2_600)
            val paused = expectMostRecentItem()
            assertTrue(paused.isSimulatorPaused)
            assertEquals(SignalQuality.STALE, paused.status.quality)

            vm.onAction(DashboardAction.OnTogglePause)
            advanceTimeBy(300)
            val resumed = expectMostRecentItem()
            assertEquals(SignalQuality.LIVE, resumed.status.quality)
            // Every accepted sample was recorded as 3 events, and the state shows the queue size.
            assertEquals(resumed.status.diagnostics.accepted * 3, resumed.queue.pending)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
