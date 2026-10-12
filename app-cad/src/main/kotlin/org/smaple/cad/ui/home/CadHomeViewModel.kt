package org.smaple.cad.ui.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.smaple.core.designsystem.component.MapleTokenState
import javax.inject.Inject

/**
 * ViewModel managing UI state for the CAD Home screen.
 */
@HiltViewModel
class CadHomeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CadHomeUiState())
    val uiState: StateFlow<CadHomeUiState> = _uiState.asStateFlow()

    fun onScanRequested() {
        _uiState.value = _uiState.value.copy(
            tokenState = MapleTokenState.Ready,
            headline = "Ready to scan",
            helperText = "Hold phone flat against the patient's card or phone"
        )
    }
}
