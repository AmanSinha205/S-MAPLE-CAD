package org.smaple.cad.ui.patient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.smaple.cad.data.model.Patient
import org.smaple.cad.data.repository.PatientRepository
import javax.inject.Inject

/** ViewModel for the Patient Found screen. */
@HiltViewModel
class PatientFoundViewModel @Inject constructor(
    private val repository: PatientRepository
) : ViewModel() {

    private val _patient = MutableStateFlow<Patient?>(null)
    val patient: StateFlow<Patient?> = _patient.asStateFlow()

    init {
        viewModelScope.launch {
            // Retrieve the most recently discovered patient, if any.
            _patient.value = repository.getLastDiscoveredPatient()
        }
    }
}
