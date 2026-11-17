package com.autolink.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TelemetryDao {

    /** Returns one row id per input; -1 where the id already existed (duplicate insert is a no-op). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertAll(events: List<TelemetryEventEntity>): List<Long>

    @Query(
        "SELECT * FROM telemetry_events WHERE status = 'PENDING' " +
            "ORDER BY occurredAtEpochMs, id LIMIT :limit",
    )
    abstract suspend fun pending(limit: Int): List<TelemetryEventEntity>

    @Query(
        "UPDATE telemetry_events SET status = 'ACKNOWLEDGED', acknowledgedAtEpochMs = :nowEpochMs " +
            "WHERE status = 'PENDING' AND id IN (:ids)",
    )
    abstract suspend fun markAcknowledged(ids: List<String>, nowEpochMs: Long): Int

    @Query("SELECT COUNT(*) FROM telemetry_events WHERE status = :status")
    abstract fun observeCount(status: String): Flow<Long>

    @Query("SELECT COUNT(*) FROM telemetry_events")
    abstract suspend fun count(): Long

    @Query("DELETE FROM telemetry_events WHERE status = 'ACKNOWLEDGED' AND acknowledgedAtEpochMs < :cutoffEpochMs")
    abstract suspend fun deleteAcknowledgedBefore(cutoffEpochMs: Long): Int

    /** Oldest first, acknowledged before pending. */
    @Query(
        "DELETE FROM telemetry_events WHERE id IN (SELECT id FROM telemetry_events " +
            "ORDER BY CASE status WHEN 'ACKNOWLEDGED' THEN 0 ELSE 1 END, occurredAtEpochMs, id LIMIT :n)",
    )
    abstract suspend fun deleteOldest(n: Long): Int

    /** Insert and enforce retention atomically, so the table can never be observed over its cap. */
    @Transaction
    open suspend fun insertAndPrune(
        events: List<TelemetryEventEntity>,
        maxEvents: Int,
        acknowledgedCutoffEpochMs: Long,
    ): InsertResult {
        val inserted = insertAll(events).count { it != -1L }
        val agedOut = deleteAcknowledgedBefore(acknowledgedCutoffEpochMs)
        val overflow = count() - maxEvents
        val evicted = if (overflow > 0) deleteOldest(overflow) else 0
        return InsertResult(inserted = inserted, deleted = agedOut + evicted)
    }
}

data class InsertResult(val inserted: Int, val deleted: Int)
