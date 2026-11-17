package com.autolink.feature.vehicle.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autolink.core.domain.vehicle.SimulatorControls
import com.autolink.core.domain.vehicle.VehicleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * Calls the repository directly: a use case that only forwarded observeStatus() would be a pass-through
 * (architecture guide §6). Add one when Car API / simulator selection logic arrives.
 */
class DashboardViewModel(
    repository: VehicleRepository,
    private val simulator: SimulatorControls?,
) : ViewModel() {

    private val showDeveloperPanel = MutableStateFlow(simulator != null)

    val state: StateFlow<DashboardState> = combine(
        repository.observeStatus(),
        simulator?.isPaused ?: MutableStateFlow(false),
        showDeveloperPanel,
    ) { status, paused, showPanel ->
        DashboardState(status = status, isSimulatorPaused = paused, showDeveloperPanel = showPanel)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardState())

    fun onAction(action: DashboardAction) {
        when (action) {
            DashboardAction.OnTogglePause -> simulator?.let { if (it.isPaused.value) it.resume() else it.pause() }
            DashboardAction.OnInjectDuplicate -> simulator?.injectDuplicate()
            DashboardAction.OnInjectOutOfOrder -> simulator?.injectOutOfOrder()
            DashboardAction.OnToggleDeveloperPanel -> showDeveloperPanel.update { !it && simulator != null }
        }
    }
}
