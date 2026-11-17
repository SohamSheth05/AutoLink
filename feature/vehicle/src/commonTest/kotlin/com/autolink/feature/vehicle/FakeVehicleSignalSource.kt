package com.autolink.feature.vehicle

import com.autolink.core.domain.vehicle.SignalSource
import com.autolink.feature.vehicle.domain.VehicleSample
import com.autolink.feature.vehicle.domain.VehicleSignalSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeVehicleSignalSource : VehicleSignalSource {
    override val kind = SignalSource.SIMULATED
    private val flow = MutableSharedFlow<VehicleSample>()
    override fun samples(): Flow<VehicleSample> = flow
    suspend fun emit(seq: Long, speed: Float) = flow.emit(VehicleSample(seq, seq * 1_000, speed, 80, 21f))
}
