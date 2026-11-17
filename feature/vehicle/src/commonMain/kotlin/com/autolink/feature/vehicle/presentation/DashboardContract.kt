package com.autolink.feature.vehicle.presentation

import com.autolink.core.domain.vehicle.SignalSource
import com.autolink.core.domain.vehicle.VehicleStatus

data class DashboardState(
    val status: VehicleStatus = VehicleStatus.unavailable(SignalSource.SIMULATED),
    val isSimulatorPaused: Boolean = false,
    val showDeveloperPanel: Boolean = true,
)

sealed interface DashboardAction {
    data object OnTogglePause : DashboardAction
    data object OnInjectDuplicate : DashboardAction
    data object OnInjectOutOfOrder : DashboardAction
    data object OnToggleDeveloperPanel : DashboardAction
}
