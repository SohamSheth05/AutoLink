package com.autolink.core.domain.telemetry

import kotlinx.coroutines.flow.Flow

/**
 * Durable, bounded outbox for telemetry. Events are written here *before* any upload attempt, so a crash or
 * an offline period never loses data that was already accepted (up to the retention limit).
 */
interface TelemetryQueue {
    /** Stores events; ids that already exist are ignored. Returns how many were newly stored. */
    suspend fun enqueue(events: List<TelemetryEvent>): Int

    /** Oldest pending events first, at most [limit]. */
    suspend fun pendingBatch(limit: Int): List<TelemetryEvent>

    /** Marks only these ids as acknowledged by the server. Unknown or already-acknowledged ids are ignored. */
    suspend fun markAcknowledged(ids: Collection<String>): Int

    fun observeStats(): Flow<TelemetryQueueStats>
}

data class TelemetryQueueStats(
    val pending: Long = 0,
    val acknowledged: Long = 0,
) {
    val total: Long get() = pending + acknowledged
}

/**
 * Eviction policy (v1):
 * 1. Acknowledged events older than [acknowledgedMaxAgeMs] are deleted (they are only local history).
 * 2. If more than [maxEvents] remain, the oldest are deleted, acknowledged ones first, then pending.
 *    Step 2 is the only way pending data is lost: a long outage keeps the newest [maxEvents].
 */
data class RetentionPolicy(
    val maxEvents: Int = 10_000,
    val acknowledgedMaxAgeMs: Long = 24 * 60 * 60 * 1000L,
) {
    init {
        require(maxEvents > 0) { "maxEvents must be positive" }
        require(acknowledgedMaxAgeMs >= 0) { "acknowledgedMaxAgeMs must not be negative" }
    }
}
