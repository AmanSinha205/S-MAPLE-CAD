package org.smaple.cad.reader

import kotlinx.coroutines.flow.StateFlow

/**
 * Represents the possible discovery states of an NFC card.
 */
sealed interface CardDiscoveryState {
    /** No scanning activity. */
    object Idle : CardDiscoveryState

    /** Scanning for a card is in progress. */
    object Scanning : CardDiscoveryState

    /** A card has been detected and a connection is established. */
    object CardDetected : CardDiscoveryState

    /** The attempt to connect failed. */
    object ConnectionFailed : CardDiscoveryState

    /** The connection was lost or cancelled. */
    object Disconnected : CardDiscoveryState
}

/**
 * Manager responsible for discovering NFC cards.
 *
 * The concrete implementation is provided via Hilt. For Milestone 1 the implementation is a fake
 * one that simulates the state transitions without touching any Android NFC APIs.
 */
interface CardReaderManager {
    /** Current discovery state exposed as a read‑only [StateFlow]. */
    val discoveryState: StateFlow<CardDiscoveryState>

    /** Starts the scanning process. */
    suspend fun startScanning()

    /** Cancels a running scan and returns to the idle state. */
    fun cancelScanning()
}
