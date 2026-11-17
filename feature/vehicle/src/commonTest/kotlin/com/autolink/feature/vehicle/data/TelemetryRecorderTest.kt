package com.autolink.feature.vehicle.data

import com.autolink.core.domain.telemetry.TelemetryType
import com.autolink.feature.vehicle.FakeTelemetryQueue
import com.autolink.feature.vehicle.data.telemetry.TelemetryRecorder
import com.autolink.feature.vehicle.domain.VehicleSample
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TelemetryRecorderTest {
    private val queue = FakeTelemetryQueue()
    private var nextId = 0
    private val recorder = TelemetryRecorder(queue, vehicleId = "demo-vehicle-001", newId = { "id-${nextId++}" })

    @Test fun one_event_per_signal_with_unique_ids_and_sample_time() = runTest {
        recorder.onAccepted(VehicleSample(seq = 1, occurredAtEpochMs = 123, speedKph = 50f, batteryPercent = 80, cabinTempC = 21.5f))
        assertEquals(
            listOf(TelemetryType.SPEED_KPH, TelemetryType.BATTERY_PERCENT, TelemetryType.CABIN_TEMP_C),
            queue.events.map { it.type },
        )
        assertEquals(3, queue.events.map { it.id }.toSet().size)
        assertEquals(setOf(123L), queue.events.map { it.occurredAtEpochMs }.toSet())
    }

    @Test fun unknown_signals_are_not_recorded_as_zero() = runTest {
        recorder.onAccepted(VehicleSample(seq = 1, occurredAtEpochMs = 0, speedKph = null, batteryPercent = 80, cabinTempC = null))
        assertEquals(listOf(TelemetryType.BATTERY_PERCENT), queue.events.map { it.type })
    }
}
