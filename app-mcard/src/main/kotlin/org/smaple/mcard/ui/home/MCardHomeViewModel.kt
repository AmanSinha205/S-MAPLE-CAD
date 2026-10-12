package org.smaple.mcard.ui.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * ViewModel managing UI state for the patient mCard Home screen.
 */
@HiltViewModel
class MCardHomeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(MCardHomeUiState())
    val uiState: StateFlow<MCardHomeUiState> = _uiState.asStateFlow()
}
