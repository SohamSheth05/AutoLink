package com.autolink.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "telemetry_events",
    indices = [Index(value = ["status", "occurredAtEpochMs"])],
)
data class TelemetryEventEntity(
    @PrimaryKey val id: String,
    val vehicleId: String,
    val type: String,
    val value: Double,
    val occurredAtEpochMs: Long,
    val status: String = UploadStatus.PENDING,
    val acknowledgedAtEpochMs: Long? = null,
)

/** Stored as text so the schema stays readable in a DB browser. */
object UploadStatus {
    const val PENDING = "PENDING"
    const val ACKNOWLEDGED = "ACKNOWLEDGED"
}
