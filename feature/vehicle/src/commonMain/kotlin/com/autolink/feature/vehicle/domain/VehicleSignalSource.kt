package com.autolink.feature.vehicle.domain

import com.autolink.core.domain.vehicle.SignalSource
import kotlinx.coroutines.flow.Flow

/** A raw stream of samples. May be delayed, duplicated or out of order; the repository reconciles. */
interface VehicleSignalSource {
    val kind: SignalSource
    fun samples(): Flow<VehicleSample>
}
