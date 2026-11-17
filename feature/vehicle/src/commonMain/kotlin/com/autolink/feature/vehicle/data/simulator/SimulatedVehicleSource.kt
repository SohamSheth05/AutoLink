package com.autolink.feature.vehicle.data.simulator

import com.autolink.core.common.time.WallClock
import com.autolink.core.domain.vehicle.SignalSource
import com.autolink.core.domain.vehicle.SimulatorControls
import com.autolink.feature.vehicle.domain.VehicleSample
import com.autolink.feature.vehicle.domain.VehicleSignalSource
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * SIMULATED vehicle: a gentle drive cycle with slowly draining battery and drifting cabin temperature.
 * Emits every [periodMs]. Pausing stops emission entirely, which is how the dashboard's stale state is demoed.
 *
 * The simulator state is shared, so all collectors see one vehicle (sequence numbers stay unique).
 */
class SimulatedVehicleSource(
    private val wallClock: WallClock,
    private val periodMs: Long = DEFAULT_PERIOD_MS,
    private val random: Random = Random(seed = 42),
) : VehicleSignalSource, SimulatorControls {

    override val kind = SignalSource.SIMULATED

    private val _isPaused = MutableStateFlow(false)
    override val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val injected = MutableSharedFlow<VehicleSample>(
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private val lock = Mutex()
    private var seq = 0L
    private var tick = 0L
    private var battery = 86.0
    private var cabin = 21.5
    private var last: VehicleSample? = null

    override fun samples(): Flow<VehicleSample> = merge(ticks(), injected)

    private fun ticks(): Flow<VehicleSample> = flow {
        while (true) {
            _isPaused.first { !it }
            emit(nextSample())
            delay(periodMs)
        }
    }

    private suspend fun nextSample(): VehicleSample = lock.withLock {
        tick++
        // ~60 s drive cycle between 0 and ~95 km/h, with a little noise.
        val phase = (tick * periodMs % CYCLE_MS).toDouble() / CYCLE_MS
        val speed = (47.5 + 47.5 * sin(2 * PI * phase - PI / 2) + random.nextDouble(-1.5, 1.5)).coerceAtLeast(0.0)
        battery = (battery - 0.01 - speed * 0.0002).coerceAtLeast(0.0)
        cabin = (cabin + random.nextDouble(-0.05, 0.05)).coerceIn(16.0, 30.0)
        VehicleSample(
            seq = ++seq,
            occurredAtEpochMs = wallClock.nowEpochMs(),
            speedKph = speed.toFloat(),
            batteryPercent = battery.toInt(),
            cabinTempC = cabin.toFloat(),
        ).also { last = it }
    }

    override fun pause() { _isPaused.value = true }
    override fun resume() { _isPaused.value = false }

    override fun injectDuplicate() {
        last?.let { injected.tryEmit(it) }
    }

    override fun injectOutOfOrder() {
        val l = last ?: return
        injected.tryEmit(
            l.copy(
                seq = (l.seq - 3).coerceAtLeast(0),
                occurredAtEpochMs = l.occurredAtEpochMs - 1_500,
                speedKph = 0f, // obviously wrong value: if it ever shows on screen, ordering is broken
            ),
        )
    }

    companion object {
        const val DEFAULT_PERIOD_MS = 500L
        private const val CYCLE_MS = 60_000L
    }
}
