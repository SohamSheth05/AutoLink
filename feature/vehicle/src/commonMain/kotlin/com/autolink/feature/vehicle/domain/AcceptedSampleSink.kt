package com.autolink.feature.vehicle.domain

/** Receives every sample the reconciler accepted (never duplicates or out-of-order ones). */
fun interface AcceptedSampleSink {
    suspend fun onAccepted(sample: VehicleSample)

    companion object {
        val None = AcceptedSampleSink { }
    }
}
