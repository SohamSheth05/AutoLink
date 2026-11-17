package com.autolink.feature.vehicle.data.telemetry

import com.autolink.core.domain.telemetry.TelemetryEvent
import com.autolink.core.domain.telemetry.TelemetryQueue
import com.autolink.core.domain.telemetry.TelemetryType
import com.autolink.feature.vehicle.domain.AcceptedSampleSink
import com.autolink.feature.vehicle.domain.VehicleSample
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Turns each accepted sample into one telemetry event per known signal and stores them durably.
 * Ids are generated here, once; uploads and retries reuse them so the backend can deduplicate.
 */
@OptIn(ExperimentalUuidApi::class)
class TelemetryRecorder(
    private val queue: TelemetryQueue,
    private val vehicleId: String,
    private val newId: () -> String = { Uuid.random().toString() },
) : AcceptedSampleSink {

    override suspend fun onAccepted(sample: VehicleSample) {
        queue.enqueue(sample.toEvents())
    }

    fun VehicleSample.toEvents(): List<TelemetryEvent> = buildList {
        // Unknown signals are skipped, never recorded as 0.
        speedKph?.let { add(event(TelemetryType.SPEED_KPH, it.toDouble())) }
        batteryPercent?.let { add(event(TelemetryType.BATTERY_PERCENT, it.toDouble())) }
        cabinTempC?.let { add(event(TelemetryType.CABIN_TEMP_C, it.toDouble())) }
    }

    private fun VehicleSample.event(type: TelemetryType, value: Double) =
        TelemetryEvent(newId(), vehicleId, type, value, occurredAtEpochMs)
}
