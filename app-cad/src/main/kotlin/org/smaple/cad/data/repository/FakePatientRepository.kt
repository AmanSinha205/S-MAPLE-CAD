package org.smaple.cad.data.repository

import kotlinx.coroutines.delay
import org.smaple.cad.data.model.Patient
import org.smaple.cad.data.model.Patient.RiskStatus
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fake repository that simulates patient discovery after an NFC scan.
 * No real networking or hardware is used – a delay mimics the latency.
 */
@Singleton
class FakePatientRepository @Inject constructor() : PatientRepository {

    private var lastPatient: Patient? = null

    override suspend fun discoverPatient(): Patient {
        // Simulate scanning delay (1.5 seconds).
        delay(1500L)
        val sample = Patient(
            patientId = "PT-100234",
            name = "John Smith",
            age = 36,
            gender = org.smaple.core.model.Gender.MALE,
            lastVisit = LocalDateTime.now().minusDays(30),
            medicalSummary = "Hypertension, Seasonal Allergies",
            riskStatus = RiskStatus.MEDIUM
        )
        lastPatient = sample
        return sample
    }

    override fun getLastDiscoveredPatient(): Patient? = lastPatient
}
