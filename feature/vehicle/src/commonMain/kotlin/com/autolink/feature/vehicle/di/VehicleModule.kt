package com.autolink.feature.vehicle.di

import com.autolink.core.domain.vehicle.SimulatorControls
import com.autolink.core.domain.vehicle.VehicleRepository
import com.autolink.feature.vehicle.data.repository.ReconcilingVehicleRepository
import com.autolink.feature.vehicle.data.simulator.SimulatedVehicleSource
import com.autolink.feature.vehicle.data.telemetry.TelemetryRecorder
import com.autolink.feature.vehicle.domain.VehicleSignalSource
import com.autolink.feature.vehicle.presentation.DashboardViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.binds
import org.koin.dsl.module

val vehicleModule = module {
    // One simulated vehicle for the whole process. A CarPropertyManager source will replace this binding.
    single { SimulatedVehicleSource(wallClock = get()) } binds arrayOf(VehicleSignalSource::class, SimulatorControls::class)
    single { TelemetryRecorder(queue = get(), vehicleId = DEMO_VEHICLE_ID) }
    single<VehicleRepository> {
        ReconcilingVehicleRepository(source = get(), clock = get(), sink = get<TelemetryRecorder>())
    }
    // getOrNull: with a real Car API source there are no simulator controls.
    viewModel { DashboardViewModel(repository = get(), telemetryQueue = get(), simulator = getOrNull()) }
}

/** Demo identifier only; never a real VIN. */
const val DEMO_VEHICLE_ID = "demo-vehicle-001"
