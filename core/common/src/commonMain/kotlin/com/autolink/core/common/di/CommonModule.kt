package com.autolink.core.common.di

import com.autolink.core.common.time.MonotonicClock
import com.autolink.core.common.time.SystemMonotonicClock
import com.autolink.core.common.time.SystemWallClock
import com.autolink.core.common.time.WallClock
import org.koin.dsl.module

val commonModule = module {
    single<MonotonicClock> { SystemMonotonicClock() }
    single<WallClock> { SystemWallClock() }
}
