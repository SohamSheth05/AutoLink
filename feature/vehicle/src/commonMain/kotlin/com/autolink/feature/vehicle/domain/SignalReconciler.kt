package com.autolink.feature.vehicle.domain

import com.autolink.core.domain.vehicle.SignalDiagnostics
import com.autolink.core.domain.vehicle.SignalSource
import com.autolink.core.domain.vehicle.VehicleStatus

/**
 * Turns an unreliable sample stream into a consistent [VehicleStatus].
 *
 * Policy (v1): last-writer-wins by sequence number.
 * - seq == last accepted -> duplicate, discarded.
 * - seq <  last accepted -> out of order, discarded (state must not regress).
 * Discarded samples do not refresh freshness: a replayed old sample is not proof the vehicle is still talking.
 *
 * Not thread-safe; callers serialise access.
 */
class SignalReconciler(
    private val source: SignalSource,
    private val freshness: SignalFreshnessMonitor = SignalFreshnessMonitor(),
) {
    private var latest: VehicleSample? = null
    private var diagnostics = SignalDiagnostics()

    enum class Decision { ACCEPTED, DUPLICATE, OUT_OF_ORDER }

    fun offer(sample: VehicleSample, receivedAtMonoMs: Long): Decision {
        val current = latest
        val decision = when {
            current == null || sample.seq > current.seq -> Decision.ACCEPTED
            sample.seq == current.seq -> Decision.DUPLICATE
            else -> Decision.OUT_OF_ORDER
        }
        diagnostics = when (decision) {
            Decision.ACCEPTED -> {
                latest = sample
                freshness.onSampleReceived(receivedAtMonoMs)
                diagnostics.copy(accepted = diagnostics.accepted + 1)
            }
            Decision.DUPLICATE -> diagnostics.copy(discardedDuplicate = diagnostics.discardedDuplicate + 1)
            Decision.OUT_OF_ORDER -> diagnostics.copy(discardedOutOfOrder = diagnostics.discardedOutOfOrder + 1)
        }
        return decision
    }

    fun snapshot(nowMonoMs: Long): VehicleStatus {
        val s = latest ?: return VehicleStatus.unavailable(source).copy(diagnostics = diagnostics)
        return VehicleStatus(
            speedKph = s.speedKph,
            batteryPercent = s.batteryPercent,
            cabinTempC = s.cabinTempC,
            updatedAtEpochMs = s.occurredAtEpochMs,
            ageMs = freshness.ageMs(nowMonoMs),
            quality = freshness.quality(nowMonoMs),
            source = source,
            diagnostics = diagnostics,
        )
    }
}
