package org.smaple.cad.reader

import kotlinx.coroutines.flow.StateFlow

/**
 * Structured reason codes for NFC connection failures.
 * Used by [CardDiscoveryState.Error] to surface actionable user messages.
 */
enum class NfcErrorReason {
    /** The device has no NFC hardware. */
    NO_NFC_HARDWARE,

    /** NFC is available but turned off in device settings. */
    NFC_DISABLED,

    /** A tag was detected but does not support ISO 14443-4 (IsoDep). */
    NOT_ISO_DEP,

    /** IsoDep connection to the card/phone could not be established. */
    CONNECTION_FAILED,

    /** Physical tap was detected, but the card moved away mid-transfer. */
    TAG_LOST,

    /** SELECT AID response was not 90 00 — wrong app or AID mismatch. */
    WRONG_AID_RESPONSE,

    /** GET_BASIC_INFO returned a non-success status word. */
    GET_BASIC_INFO_FAILED,

    /** The codec could not decode the response payload bytes. */
    DECODE_FAILED,

    /** Any other unexpected communication failure. */
    UNKNOWN,
}

/**
 * Represents the possible discovery states of an NFC card.
 */
sealed interface CardDiscoveryState {
    /** No scanning activity. */
    object Idle : CardDiscoveryState

    /** Scanning for a card is in progress. */
    object Scanning : CardDiscoveryState

    /** A card has been detected and the IsoDep connection is being negotiated. */
    object CardDetected : CardDiscoveryState

    /**
     * Communication failed with a structured reason and optional detail message.
     * Replaces the generic [ConnectionFailed] singleton for production error handling.
     */
    data class Error(
        val reason: NfcErrorReason,
        val detail: String? = null,
    ) : CardDiscoveryState

    /**
     * Generic connection failure — kept for backward compatibility with FakeCardReaderManager
     * and existing tests. Prefer [Error] with a specific [NfcErrorReason] in new code.
     */
    object ConnectionFailed : CardDiscoveryState

    /** The connection was lost or cancelled. */
    object Disconnected : CardDiscoveryState
}

/**
 * Manager responsible for discovering NFC cards.
 *
 * The concrete implementation is provided via Hilt. For Milestone 1 the implementation is a fake
 * one that simulates the state transitions without touching any Android NFC APIs.
 */
interface CardReaderManager {
    /** Current discovery state exposed as a read‑only [StateFlow]. */
    val discoveryState: StateFlow<CardDiscoveryState>

    /** Starts the scanning process. */
    suspend fun startScanning()

    /** Cancels a running scan and returns to the idle state. */
    fun cancelScanning()

    /**
     * Reads basic info from the card using the GET_BASIC_INFO APDU protocol.
     * Throws [UnsupportedOperationException] by default if not supported by the underlying reader.
     */
    suspend fun getBasicInfo(): org.smaple.core.model.BasicInfo {
        throw UnsupportedOperationException("getBasicInfo is not supported by this CardReaderManager")
    }
}
