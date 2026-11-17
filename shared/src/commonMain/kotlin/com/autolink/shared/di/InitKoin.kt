package com.autolink.shared.di

import com.autolink.core.common.di.commonModule
import com.autolink.feature.vehicle.di.vehicleModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

val appModules = listOf(commonModule, vehicleModule)

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(appModules)
    }
}
