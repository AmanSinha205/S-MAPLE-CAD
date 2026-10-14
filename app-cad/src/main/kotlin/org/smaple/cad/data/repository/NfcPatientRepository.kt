package org.smaple.cad.data.repository

import org.smaple.cad.data.model.Patient
import org.smaple.cad.reader.CardReaderManager
import org.smaple.core.model.BasicInfo
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository implementation that coordinates with [CardReaderManager] to discover
 * and retrieve patient information from an mCard via NFC.
 *
 * Replaces [FakePatientRepository] in real card-reading flows.
 */
@Singleton
class NfcPatientRepository @Inject constructor(
    private val cardReaderManager: CardReaderManager
) : PatientRepository {

    private var lastPatient: Patient? = null

    override suspend fun discoverPatient(): Patient {
        val basicInfo = cardReaderManager.getBasicInfo()
        val patient = toPatient(basicInfo)
        lastPatient = patient
        return patient
    }

    override fun getLastDiscoveredPatient(): Patient? = lastPatient

    /**
     * Converts card [BasicInfo] domain model into the CAD workflow [Patient] model.
     */
    fun toPatient(basicInfo: BasicInfo): Patient {
        return Patient(
            patientId = basicInfo.patientId,
            name = basicInfo.name,
            age = basicInfo.calculateAge(),
            gender = basicInfo.gender,
            lastVisit = LocalDateTime.now(),
            medicalSummary = "Blood Group: ${basicInfo.bloodGroup.displayLabel}",
            riskStatus = Patient.RiskStatus.LOW
        )
    }
}
