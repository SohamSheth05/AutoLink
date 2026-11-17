package com.autolink.feature.vehicle.domain

/**
 * One raw reading from a vehicle source.
 *
 * @param seq strictly increasing per source. Ordering uses this, not timestamps: wall clocks can jump,
 *   and two samples can share a millisecond.
 */
data class VehicleSample(
    val seq: Long,
    val occurredAtEpochMs: Long,
    val speedKph: Float?,
    val batteryPercent: Int?,
    val cabinTempC: Float?,
)
