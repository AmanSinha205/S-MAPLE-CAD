package org.smaple.cad.reader

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fake implementation of [CardReaderManager] that simulates NFC card discovery.
 *
 * The state machine is:
 *   Idle -> Scanning (when [startScanning] is called) -> after 2 seconds -> CardDetected.
 *   The other states (ConnectionFailed, Disconnected) are not emitted in this simple simulation
 *   but the contract allows them to be used later.
 */
@Singleton
class FakeCardReaderManager @Inject constructor() : CardReaderManager {
    private val _discoveryState = MutableStateFlow<CardDiscoveryState>(CardDiscoveryState.Idle)
    override val discoveryState: StateFlow<CardDiscoveryState> = _discoveryState

    private var scanningJob: kotlinx.coroutines.Job? = null

    override suspend fun startScanning() {
        // If already scanning or detected, ignore.
        if (_discoveryState.value != CardDiscoveryState.Idle) return
        _discoveryState.value = CardDiscoveryState.Scanning
        // Simulate scanning delay on a background coroutine.
        scanningJob = CoroutineScope(Dispatchers.Default).launch {
            delay(2000L) // 2 seconds simulated scan time
            _discoveryState.value = CardDiscoveryState.CardDetected
        }
    }

    override fun cancelScanning() {
        scanningJob?.cancel()
        _discoveryState.value = CardDiscoveryState.Idle
    }
}
