package com.autolink.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformDatabaseModule: Module = module {
    single<RoomDatabase.Builder<AutoLinkDatabase>> {
        val context = androidContext()
        Room.databaseBuilder<AutoLinkDatabase>(
            context = context,
            name = context.getDatabasePath(DATABASE_NAME).absolutePath,
        )
    }
}
