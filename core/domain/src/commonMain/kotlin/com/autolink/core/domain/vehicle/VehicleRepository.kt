package com.autolink.core.domain.vehicle

import kotlinx.coroutines.flow.Flow

/**
 * App-facing contract. Returns domain objects only: no VHAL property IDs, no Car API types.
 * Implementations: a simulator (default) and, later, a CarPropertyManager adapter.
 */
interface VehicleRepository {
    fun observeStatus(): Flow<VehicleStatus>
}
