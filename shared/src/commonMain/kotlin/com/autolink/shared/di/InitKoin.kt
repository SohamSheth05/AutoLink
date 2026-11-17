package com.autolink.shared.di

import com.autolink.core.common.di.commonModule
import com.autolink.core.data.di.dataModule
import com.autolink.core.database.databaseModule
import com.autolink.core.database.platformDatabaseModule
import com.autolink.feature.vehicle.di.vehicleModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

val appModules = listOf(commonModule, databaseModule, platformDatabaseModule, dataModule, vehicleModule)

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(appModules)
    }
}
