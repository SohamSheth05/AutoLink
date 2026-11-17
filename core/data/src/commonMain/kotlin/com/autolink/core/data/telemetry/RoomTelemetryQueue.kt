package com.autolink.core.data.telemetry

import com.autolink.core.common.time.WallClock
import com.autolink.core.database.TelemetryDao
import com.autolink.core.database.TelemetryEventEntity
import com.autolink.core.database.UploadStatus
import com.autolink.core.domain.telemetry.RetentionPolicy
import com.autolink.core.domain.telemetry.TelemetryEvent
import com.autolink.core.domain.telemetry.TelemetryQueue
import com.autolink.core.domain.telemetry.TelemetryQueueStats
import com.autolink.core.domain.telemetry.TelemetryType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class RoomTelemetryQueue(
    private val dao: TelemetryDao,
    private val wallClock: WallClock,
    private val retention: RetentionPolicy = RetentionPolicy(),
) : TelemetryQueue {

    override suspend fun enqueue(events: List<TelemetryEvent>): Int {
        if (events.isEmpty()) return 0
        val result = dao.insertAndPrune(
            events = events.map { it.toEntity() },
            maxEvents = retention.maxEvents,
            acknowledgedCutoffEpochMs = wallClock.nowEpochMs() - retention.acknowledgedMaxAgeMs,
        )
        return result.inserted
    }

    override suspend fun pendingBatch(limit: Int): List<TelemetryEvent> {
        require(limit > 0) { "limit must be positive" }
        // Rows with a type this app version doesn't know (e.g. after a downgrade) are skipped, not crashed on.
        return dao.pending(limit).mapNotNull { it.toDomain() }
    }

    override suspend fun markAcknowledged(ids: Collection<String>): Int {
        if (ids.isEmpty()) return 0
        // SQLite caps bound parameters (999 on older versions); chunk to stay well under it.
        return ids.distinct().chunked(500).sumOf { dao.markAcknowledged(it, wallClock.nowEpochMs()) }
    }

    override fun observeStats(): Flow<TelemetryQueueStats> = combine(
        dao.observeCount(UploadStatus.PENDING),
        dao.observeCount(UploadStatus.ACKNOWLEDGED),
    ) { pending, acknowledged -> TelemetryQueueStats(pending = pending, acknowledged = acknowledged) }
}

internal fun TelemetryEvent.toEntity() = TelemetryEventEntity(
    id = id,
    vehicleId = vehicleId,
    type = type.wireName,
    value = value,
    occurredAtEpochMs = occurredAtEpochMs,
)

internal fun TelemetryEventEntity.toDomain(): TelemetryEvent? {
    val type = TelemetryType.fromWireName(type) ?: return null
    return TelemetryEvent(id, vehicleId, type, value, occurredAtEpochMs)
}
