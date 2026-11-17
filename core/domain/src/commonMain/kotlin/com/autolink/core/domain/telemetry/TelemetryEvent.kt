package com.autolink.core.domain.telemetry

/** What a telemetry value measures. [wireName] is the stable name used in storage and the backend API. */
enum class TelemetryType(val wireName: String) {
    SPEED_KPH("speed_kph"),
    BATTERY_PERCENT("battery_percent"),
    CABIN_TEMP_C("cabin_temp_c"),
    ;

    companion object {
        fun fromWireName(name: String): TelemetryType? = entries.firstOrNull { it.wireName == name }
    }
}

/**
 * One telemetry measurement queued for upload.
 *
 * @param id stable UUID generated once when the event is created. Retries re-send the same id, which is
 *   what lets the backend deduplicate (at-least-once delivery + idempotent server = effectively once).
 * @param vehicleId demo identifier only; never a real VIN.
 */
data class TelemetryEvent(
    val id: String,
    val vehicleId: String,
    val type: TelemetryType,
    val value: Double,
    val occurredAtEpochMs: Long,
)
