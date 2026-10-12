package org.smaple.cad.data.repository

import org.smaple.cad.data.model.Patient

/** Repository abstraction for patient discovery. */
interface PatientRepository {
    /** Simulates discovering a patient via NFC scanning. */
    suspend fun discoverPatient(): Patient
    /** Returns the most recently discovered patient, or null if none. */
    fun getLastDiscoveredPatient(): Patient?
}
