package com.autolink.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.module.Module
import org.koin.dsl.module

@Database(entities = [TelemetryEventEntity::class], version = 1, exportSchema = true)
@ConstructedBy(AutoLinkDatabaseConstructor::class)
abstract class AutoLinkDatabase : RoomDatabase() {
    abstract fun telemetryDao(): TelemetryDao
}

// Room's KSP processor generates the actual implementations.
@Suppress("NO_ACTUAL_FOR_EXPECT", "KotlinNoActualForExpect")
expect object AutoLinkDatabaseConstructor : RoomDatabaseConstructor<AutoLinkDatabase> {
    override fun initialize(): AutoLinkDatabase
}

/** Same driver and dispatcher on every platform: the bundled SQLite behaves identically in tests and on device. */
fun RoomDatabase.Builder<AutoLinkDatabase>.buildAutoLink(): AutoLinkDatabase =
    setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()

/** Provides the platform `RoomDatabase.Builder<AutoLinkDatabase>`. */
expect val platformDatabaseModule: Module

val databaseModule = module {
    single { get<RoomDatabase.Builder<AutoLinkDatabase>>().buildAutoLink() }
    single { get<AutoLinkDatabase>().telemetryDao() }
}

const val DATABASE_NAME = "autolink.db"
