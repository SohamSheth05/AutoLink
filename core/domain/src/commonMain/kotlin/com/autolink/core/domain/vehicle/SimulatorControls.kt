package com.autolink.core.domain.vehicle

import kotlinx.coroutines.flow.StateFlow

/** Developer-only fault injection. Exists only while the simulated source is active. */
interface SimulatorControls {
    val isPaused: StateFlow<Boolean>
    fun pause()
    fun resume()

    /** Re-sends the last sample with the same sequence number. */
    fun injectDuplicate()

    /** Sends a sample older than the last one, with different values, to prove state does not regress. */
    fun injectOutOfOrder()
}
