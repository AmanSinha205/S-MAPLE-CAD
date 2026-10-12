package org.smaple.cad.ui.scan

/** UI state for the Scan screen */
sealed interface ScanUiState {
    object Idle : ScanUiState
    object Scanning : ScanUiState
}
