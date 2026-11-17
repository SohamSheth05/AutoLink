package com.autolink.feature.vehicle.data.repository

import com.autolink.core.common.time.MonotonicClock
import com.autolink.core.domain.vehicle.VehicleRepository
import com.autolink.core.domain.vehicle.VehicleStatus
import com.autolink.feature.vehicle.domain.AcceptedSampleSink
import com.autolink.feature.vehicle.domain.SignalFreshnessMonitor
import com.autolink.feature.vehicle.domain.SignalReconciler
import com.autolink.feature.vehicle.domain.SignalReconciler.Decision
import com.autolink.feature.vehicle.domain.VehicleSignalSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Wraps any [VehicleSignalSource] (simulator today, Car API later) with ordering and freshness rules.
 *
 * Emits on every accepted sample and re-evaluates freshness every [freshnessTickMs], because "stale" is
 * caused by the *absence* of events, so something other than an event must notice it.
 *
 * Accepted samples go to [sink] (the durable telemetry queue) before the UI sees them. Recording happens
 * only while someone observes this flow; a foreground service will own collection once sync lands (M4).
 */
class ReconcilingVehicleRepository(
    private val source: VehicleSignalSource,
    private val clock: MonotonicClock,
    private val staleAfterMs: Long = SignalFreshnessMonitor.DEFAULT_STALE_AFTER_MS,
    private val freshnessTickMs: Long = 250L,
    private val sink: AcceptedSampleSink = AcceptedSampleSink.None,
) : VehicleRepository {

    override fun observeStatus(): Flow<VehicleStatus> = channelFlow {
        val reconciler = SignalReconciler(source.kind, SignalFreshnessMonitor(staleAfterMs))
        val lock = Mutex()
        suspend fun emitSnapshot() = send(lock.withLock { reconciler.snapshot(clock.nowMs()) })

        emitSnapshot()
        launch {
            source.samples().collect { sample ->
                val decision = lock.withLock { reconciler.offer(sample, clock.nowMs()) }
                if (decision == Decision.ACCEPTED) sink.onAccepted(sample)
                emitSnapshot() // also after discards, so the diagnostics counters update
            }
        }
        launch {
            while (true) {
                delay(freshnessTickMs)
                emitSnapshot()
            }
        }
        awaitClose()
    }.distinctUntilChanged()
}
