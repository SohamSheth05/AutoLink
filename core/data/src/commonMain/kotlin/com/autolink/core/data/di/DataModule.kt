package com.autolink.core.data.di

import com.autolink.core.data.telemetry.RoomTelemetryQueue
import com.autolink.core.domain.telemetry.TelemetryQueue
import org.koin.dsl.module

val dataModule = module {
    single<TelemetryQueue> { RoomTelemetryQueue(dao = get(), wallClock = get()) }
}
