package com.autolink.shared

import com.autolink.core.domain.vehicle.SimulatorControls
import com.autolink.core.domain.vehicle.VehicleRepository
import com.autolink.shared.di.appModules
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertSame

/** Resolves the real graph, so a missing binding fails here instead of at app start. */
class KoinGraphTest {
    @AfterTest fun tearDown() = stopKoin()

    @Test
    fun graph_resolves_and_simulator_is_one_instance() {
        val koin = startKoin { modules(appModules) }.koin
        assertNotNull(koin.get<VehicleRepository>())
        val controls = koin.get<SimulatorControls>()
        assertSame<Any>(controls, koin.get<com.autolink.feature.vehicle.domain.VehicleSignalSource>())
    }
}
