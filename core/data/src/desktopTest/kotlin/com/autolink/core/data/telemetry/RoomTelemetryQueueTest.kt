package com.autolink.core.data.telemetry

import com.autolink.core.common.time.WallClock
import com.autolink.core.database.AutoLinkDatabase
import com.autolink.core.database.buildAutoLink
import com.autolink.core.domain.telemetry.RetentionPolicy
import com.autolink.core.domain.telemetry.TelemetryEvent
import com.autolink.core.domain.telemetry.TelemetryQueueStats
import com.autolink.core.domain.telemetry.TelemetryType
import androidx.room.Room
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Real Room + bundled SQLite, in memory. No mocks: this is what runs on the device. */
class RoomTelemetryQueueTest {
    private val db: AutoLinkDatabase = Room.inMemoryDatabaseBuilder<AutoLinkDatabase>().buildAutoLink()
    private var now = 1_000_000L
    private fun queue(retention: RetentionPolicy = RetentionPolicy()) =
        RoomTelemetryQueue(db.telemetryDao(), WallClock { now }, retention)

    private fun event(id: String, at: Long = 0) =
        TelemetryEvent(id, "demo-vehicle-001", TelemetryType.SPEED_KPH, 42.5, occurredAtEpochMs = at)

    @AfterTest fun tearDown() = db.close()

    @Test fun duplicate_ids_are_stored_once() = runTest {
        val q = queue()
        assertEquals(2, q.enqueue(listOf(event("a"), event("b"))))
        assertEquals(0, q.enqueue(listOf(event("a"))))
        assertEquals(listOf("a", "b"), q.pendingBatch(10).map { it.id })
    }

    @Test fun pending_batch_is_oldest_first_and_bounded() = runTest {
        val q = queue()
        q.enqueue(listOf(event("late", at = 30), event("early", at = 10), event("mid", at = 20)))
        assertEquals(listOf("early", "mid"), q.pendingBatch(2).map { it.id })
    }

    @Test fun only_listed_ids_are_acknowledged() = runTest {
        val q = queue()
        q.enqueue(listOf(event("a", 1), event("b", 2), event("c", 3)))
        assertEquals(2, q.markAcknowledged(listOf("a", "c", "unknown")))
        assertEquals(0, q.markAcknowledged(listOf("a"))) // already acknowledged: idempotent
        assertEquals(listOf("b"), q.pendingBatch(10).map { it.id })
    }

    @Test fun count_cap_evicts_acknowledged_before_pending() = runTest {
        val q = queue(RetentionPolicy(maxEvents = 3))
        q.enqueue(listOf(event("ack-old", 1), event("p1", 2), event("p2", 3)))
        q.markAcknowledged(listOf("ack-old"))
        q.enqueue(listOf(event("p3", 4)))
        // Over the cap by one: the acknowledged row goes, even though it is not the only old one.
        assertEquals(listOf("p1", "p2", "p3"), q.pendingBatch(10).map { it.id })

        q.enqueue(listOf(event("p4", 5)))
        // Only pending rows left: the oldest pending is evicted (a long outage keeps the newest).
        assertEquals(listOf("p2", "p3", "p4"), q.pendingBatch(10).map { it.id })
    }

    @Test fun acknowledged_history_ages_out_but_pending_does_not() = runTest {
        val q = queue(RetentionPolicy(acknowledgedMaxAgeMs = 60_000))
        q.enqueue(listOf(event("a", 1), event("p", 2)))
        q.markAcknowledged(listOf("a"))
        now += 120_000
        q.enqueue(listOf(event("trigger", 3))) // pruning runs with each insert
        q.awaitStats(TelemetryQueueStats(pending = 2, acknowledged = 0))
    }

    @Test fun stats_track_pending_and_acknowledged() = runTest {
        val q = queue()
        q.awaitStats(TelemetryQueueStats())
        q.enqueue(listOf(event("a"), event("b")))
        q.awaitStats(TelemetryQueueStats(pending = 2))
        q.markAcknowledged(listOf("a"))
        q.awaitStats(TelemetryQueueStats(pending = 1, acknowledged = 1))
    }
}

/**
 * Room re-queries on its own IO thread, so wait in real time (not the test's virtual time) until the
 * flow reports [expected]. Fails after 5 s instead of hanging.
 */
private suspend fun RoomTelemetryQueue.awaitStats(expected: TelemetryQueueStats) =
    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
        kotlinx.coroutines.withTimeout(5_000) { observeStats().first { it == expected } }
    }
