package org.smaple.cad.ui.scan

import org.smaple.cad.reader.NfcErrorReason

/** UI state for the Scan screen */
sealed interface ScanUiState {
    /** Initial state — ready to scan, showing Start Scan button. */
    object Idle : ScanUiState

    /** NFC reader mode is active and waiting for a card/phone. */
    object Scanning : ScanUiState

    /**
     * A physical NFC tag was detected and the APDU handshake is in progress.
     * UI shows a "communicating…" indicator to avoid user moving phones apart.
     */
    object Communicating : ScanUiState

    /**
     * An NFC error occurred with a structured reason and user-facing message.
     * @param reason machine-readable failure code
     * @param userMessage actionable human-readable string for the UI
     */
    data class NfcError(
        val reason: NfcErrorReason,
        val userMessage: String,
    ) : ScanUiState
}

/** Maps [NfcErrorReason] to a user-facing string shown in the Scan screen. */
fun NfcErrorReason.toUserMessage(): String = when (this) {
    NfcErrorReason.NO_NFC_HARDWARE ->
        "This device does not have NFC hardware. Use a device with NFC to scan patient cards."
    NfcErrorReason.NFC_DISABLED ->
        "NFC is turned off. Please enable NFC in device Settings and try again."
    NfcErrorReason.NOT_ISO_DEP ->
        "The tapped card or device does not support ISO 14443-4. Ensure the patient is using the S-MAPLE mCard app."
    NfcErrorReason.CONNECTION_FAILED ->
        "Could not connect to the patient card. Hold the devices steady and try again."
    NfcErrorReason.TAG_LOST ->
        "Connection lost mid-scan. Keep the phones touching until the scan completes."
    NfcErrorReason.WRONG_AID_RESPONSE ->
        "S-MAPLE app not found on patient phone. Ensure mCard is installed and NFC is active."
    NfcErrorReason.GET_BASIC_INFO_FAILED ->
        "Patient data is not available on the card. Please ask patient to verify the mCard app."
    NfcErrorReason.DECODE_FAILED ->
        "Patient record could not be read — data may be corrupted. Contact support."
    NfcErrorReason.UNKNOWN ->
        "An unexpected error occurred during NFC scan. Please try again."
}
