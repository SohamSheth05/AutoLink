package com.autolink.core.domain.vehicle

/** Freshness of the snapshot as a whole. v1 is snapshot-level; per-signal freshness is future work. */
enum class SignalQuality { LIVE, STALE, UNAVAILABLE }

/** Where the values came from. The UI must always show this; simulated data is never presented as real. */
enum class SignalSource { SIMULATED, CAR_API }

/**
 * What the app knows about the vehicle right now. Values are nullable: "unknown" is never shown as 0.
 *
 * @param updatedAtEpochMs wall-clock time of the last accepted sample, for display only (null before the first one).
 * @param ageMs monotonic time since the last accepted sample (null before the first one).
 */
data class VehicleStatus(
    val speedKph: Float?,
    val batteryPercent: Int?,
    val cabinTempC: Float?,
    val updatedAtEpochMs: Long?,
    val ageMs: Long?,
    val quality: SignalQuality,
    val source: SignalSource,
    val diagnostics: SignalDiagnostics = SignalDiagnostics(),
) {
    companion object {
        fun unavailable(source: SignalSource) = VehicleStatus(
            speedKph = null,
            batteryPercent = null,
            cabinTempC = null,
            updatedAtEpochMs = null,
            ageMs = null,
            quality = SignalQuality.UNAVAILABLE,
            source = source,
        )
    }
}

/** Counters that make event-ordering decisions observable (the guide's "discarded-event metrics"). */
data class SignalDiagnostics(
    val accepted: Long = 0,
    val discardedDuplicate: Long = 0,
    val discardedOutOfOrder: Long = 0,
)
