package org.smaple.cad.ui.scan

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.smaple.cad.data.model.Patient
import org.smaple.cad.data.repository.PatientRepository
import org.smaple.cad.reader.CardDiscoveryState
import org.smaple.cad.reader.CardReaderManager
import org.smaple.cad.reader.NfcErrorReason
import javax.inject.Inject

/**
 * ViewModel for the Scan screen.
 *
 * Coordinates NFC reader mode lifecycle via [CardReaderManager] and patient
 * retrieval via [PatientRepository].
 *
 * ## Lifecycle contract
 * - Call [startScanAndDiscover] when the Scan screen becomes visible.
 * - Call [cancelScan] from `onDispose` / `DisposableEffect` to stop reader mode.
 * - The ViewModel's [viewModelScope] is cancelled when the ViewModel is cleared,
 *   which propagates cancellation to [startScanAndDiscover]'s coroutine and
 *   triggers [CardReaderManager.cancelScanning] automatically.
 *
 * ## Error states
 * Failures produce [ScanUiState.NfcError] with a user-facing message derived
 * from [NfcErrorReason.toUserMessage]. The UI can offer a "Retry" action that
 * calls [retryLastScan].
 */
@HiltViewModel
class ScanViewModel @Inject constructor(
    private val cardReaderManager: CardReaderManager,
    private val patientRepository: PatientRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private val _patient = MutableStateFlow<Patient?>(null)
    val patient: StateFlow<Patient?> = _patient.asStateFlow()

    init {
        // Mirror CardReaderManager discovery state transitions into UI state.
        // Note: we only map states here; final success / error are also handled
        // in startScanAndDiscover to carry full context.
        viewModelScope.launch {
            cardReaderManager.discoveryState.collect { discovery ->
                val newState: ScanUiState = when (discovery) {
                    CardDiscoveryState.Idle -> ScanUiState.Idle
                    CardDiscoveryState.Scanning -> ScanUiState.Scanning
                    CardDiscoveryState.CardDetected -> ScanUiState.Communicating
                    CardDiscoveryState.ConnectionFailed ->
                        ScanUiState.NfcError(NfcErrorReason.UNKNOWN, NfcErrorReason.UNKNOWN.toUserMessage())
                    CardDiscoveryState.Disconnected -> ScanUiState.Idle
                    is CardDiscoveryState.Error ->
                        ScanUiState.NfcError(discovery.reason, discovery.reason.toUserMessage())
                }
                // Don't overwrite a successfully navigated-away state
                if (_patient.value == null) {
                    _uiState.value = newState
                }
            }
        }
    }

    /**
     * Starts NFC reader mode and waits for patient discovery.
     * Navigation is driven by the [patient] flow — the Scan screen observes it
     * via [LaunchedEffect] and navigates when non-null.
     */
    fun startScanAndDiscover() {
        _patient.value = null
        viewModelScope.launch {
            try {
                cardReaderManager.startScanning()
                Log.i(TAG, "Awaiting patient discovery via NFC...")
                val discovered = patientRepository.discoverPatient()
                _patient.value = discovered
                Log.i(TAG, "Patient discovered: ${discovered.name} (ID=${discovered.patientId})")
            } catch (e: CancellationException) {
                // Normal coroutine cancellation (user tapped Cancel or left the screen)
                Log.d(TAG, "Scan coroutine cancelled normally")
                throw e  // re-throw so the coroutine framework handles it properly
            } catch (e: Exception) {
                Log.e(TAG, "Patient discovery failed: ${e.message}", e)
                // State is already set by the discoveryState collector above via CardDiscoveryState.Error;
                // fall back to UNKNOWN if the error came from the repository layer above NFC.
                if (_uiState.value !is ScanUiState.NfcError) {
                    _uiState.value = ScanUiState.NfcError(
                        NfcErrorReason.UNKNOWN,
                        NfcErrorReason.UNKNOWN.toUserMessage()
                    )
                }
            }
        }
    }

    /** Cancels any active NFC scan and returns to Idle state. */
    fun cancelScan() {
        Log.d(TAG, "cancelScan() called by user")
        cardReaderManager.cancelScanning()
        _uiState.value = ScanUiState.Idle
        _patient.value = null
    }

    /**
     * Resets to Idle and re-starts a scan.
     * Useful when bound to a "Retry" button shown in [ScanUiState.NfcError].
     */
    fun retryLastScan() {
        Log.d(TAG, "retryLastScan() — resetting to Idle and restarting")
        cancelScan()
        startScanAndDiscover()
    }

    override fun onCleared() {
        super.onCleared()
        // Ensure reader mode is released when the ViewModel is destroyed
        // (e.g., screen popped from back-stack while scanning).
        cardReaderManager.cancelScanning()
        Log.d(TAG, "ViewModel cleared — NFC reader mode disabled")
    }

    companion object {
        private const val TAG = "ScanViewModel"
    }
}
