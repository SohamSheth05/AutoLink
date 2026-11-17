package com.autolink.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

/** The desktop target only runs tests, so it gets a fresh in-memory database. */
actual val platformDatabaseModule: Module = module {
    single<RoomDatabase.Builder<AutoLinkDatabase>> { Room.inMemoryDatabaseBuilder<AutoLinkDatabase>() }
}
