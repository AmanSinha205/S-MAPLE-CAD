package org.smaple.cad.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.smaple.cad.reader.CardReaderManager
import org.smaple.cad.data.repository.PatientRepository
import org.smaple.cad.data.model.Patient
import javax.inject.Inject

/**
 * ViewModel for the Scan screen.
 * It coordinates the fake NFC scanning (via CardReaderManager) and patient discovery (via PatientRepository).
 */
@HiltViewModel
class ScanViewModel @Inject constructor(
    private val cardReaderManager: CardReaderManager,
    private val patientRepository: PatientRepository
) : ViewModel() {

    // UI state derived from CardReaderManager discovery state
    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    // Holds the discovered patient once available
    private val _patient = MutableStateFlow<Patient?>(null)
    val patient: StateFlow<Patient?> = _patient.asStateFlow()

    init {
        // Observe discovery state and map to UI state
        viewModelScope.launch {
            cardReaderManager.discoveryState.collect { discovery ->
                val state = when (discovery) {
                    CardReaderManager.CardDiscoveryState.Idle -> ScanUiState.Idle
                    CardReaderManager.CardDiscoveryState.Scanning -> ScanUiState.Scanning
                    CardReaderManager.CardDiscoveryState.CardDetected -> ScanUiState.Scanning // keep scanning UI until patient loads
                    else -> ScanUiState.Idle
                }
                _uiState.value = state
            }
        }
    }

    /** Starts the scanning process and triggers patient discovery. */
    fun startScanAndDiscover() {
        viewModelScope.launch {
            // Begin NFC scanning simulation
            cardReaderManager.startScanning()
            // Immediately set UI to scanning (in case manager hasn't emitted yet)
            _uiState.value = ScanUiState.Scanning
            // Simulate patient discovery after a short delay via repository
            val discovered = patientRepository.discoverPatient()
            _patient.value = discovered
            // After patient is ready, we can consider the card detected.
            // No explicit navigation here – UI will observe `patient` flow.
        }
    }

    fun cancelScan() {
        viewModelScope.launch {
            cardReaderManager.cancelScanning()
            _uiState.value = ScanUiState.Idle
            _patient.value = null
        }
    }
}
