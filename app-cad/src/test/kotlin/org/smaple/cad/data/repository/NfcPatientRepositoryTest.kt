package org.smaple.cad.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.smaple.cad.reader.CardDiscoveryState
import org.smaple.cad.reader.CardReaderManager
import org.smaple.core.model.BasicInfo
import org.smaple.core.model.BloodGroup
import org.smaple.core.model.Gender
import java.time.LocalDate

class NfcPatientRepositoryTest {

    private class TestCardReaderManager(
        var basicInfoResult: BasicInfo? = null,
        var shouldThrow: Boolean = false
    ) : CardReaderManager {
        override val discoveryState: StateFlow<CardDiscoveryState> =
            MutableStateFlow(CardDiscoveryState.Idle)

        override suspend fun startScanning() {}
        override fun cancelScanning() {}

        override suspend fun getBasicInfo(): BasicInfo {
            if (shouldThrow) {
                throw IllegalStateException("Card read error")
            }
            return basicInfoResult ?: throw IllegalStateException("No card present")
        }
    }

    @Test
    fun testDiscoverPatientConvertsBasicInfoCorrectly() {
        runBlocking {
            val dob = LocalDate.of(1980, 6, 15)
            val basicInfo = BasicInfo(
                patientId = "PT-8899",
                name = "Asha Verma",
                gender = Gender.FEMALE,
                bloodGroup = BloodGroup.B_POSITIVE,
                dateOfBirth = dob
            )
            val readerManager = TestCardReaderManager(basicInfoResult = basicInfo)
            val repository = NfcPatientRepository(readerManager)

            assertNull(repository.getLastDiscoveredPatient())

            val patient = repository.discoverPatient()

            assertEquals("PT-8899", patient.patientId)
            assertEquals("Asha Verma", patient.name)
            assertEquals(Gender.FEMALE, patient.gender)
            assertEquals(basicInfo.calculateAge(), patient.age)
            assertEquals("Blood Group: B+", patient.medicalSummary)
            assertNotNull(patient.lastVisit)

            assertEquals(patient, repository.getLastDiscoveredPatient())
        }
    }

    @Test(expected = IllegalStateException::class)
    fun testDiscoverPatientPropagatesException() {
        runBlocking {
            val readerManager = TestCardReaderManager(shouldThrow = true)
            val repository = NfcPatientRepository(readerManager)

            repository.discoverPatient()
        }
    }
}
