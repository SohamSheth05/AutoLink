package com.autolink.feature.vehicle

import com.autolink.core.domain.telemetry.TelemetryEvent
import com.autolink.core.domain.telemetry.TelemetryQueue
import com.autolink.core.domain.telemetry.TelemetryQueueStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeTelemetryQueue : TelemetryQueue {
    val events = mutableListOf<TelemetryEvent>()
    private val stats = MutableStateFlow(TelemetryQueueStats())

    override suspend fun enqueue(events: List<TelemetryEvent>): Int {
        val fresh = events.filter { e -> this.events.none { it.id == e.id } }
        this.events += fresh
        stats.value = TelemetryQueueStats(pending = this.events.size.toLong())
        return fresh.size
    }

    override suspend fun pendingBatch(limit: Int) = events.take(limit)
    override suspend fun markAcknowledged(ids: Collection<String>) = 0
    override fun observeStats(): Flow<TelemetryQueueStats> = stats
}
