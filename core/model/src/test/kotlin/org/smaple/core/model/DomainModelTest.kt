package org.smaple.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class DomainModelTest {

    @Test
    fun testGenderCodeMapping() {
        assertEquals(Gender.MALE, Gender.fromCode(0))
        assertEquals(Gender.FEMALE, Gender.fromCode(1))
        assertEquals(Gender.NON_BINARY, Gender.fromCode(2))
        assertEquals(Gender.NOT_STATED, Gender.fromCode(3))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testInvalidGenderCodeThrows() {
        Gender.fromCode(99)
    }

    @Test
    fun testBloodGroupCodeMapping() {
        assertEquals(BloodGroup.UNKNOWN, BloodGroup.fromCode(0))
        assertEquals(BloodGroup.A_POSITIVE, BloodGroup.fromCode(1))
        assertEquals(BloodGroup.O_NEGATIVE, BloodGroup.fromCode(8))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testInvalidBloodGroupCodeThrows() {
        BloodGroup.fromCode(10)
    }

    @Test
    fun testBloodPressureValidRanges() {
        val bp = BloodPressure(systolic = 120, diastolic = 80)
        assertEquals("120/80", bp.formatted())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testBloodPressureOutOfRangeThrows() {
        BloodPressure(systolic = 300, diastolic = 80)
    }

    @Test
    fun testVitalSignsValidation() {
        val vitals = VitalSigns(
            bloodPressure = BloodPressure(120, 80),
            temperatureCelsius = 37.0f,
            heartRateBpm = 72
        )
        assertTrue(vitals.hasAnyMeasurement)
    }

    @Test
    fun testPatientDemographicsAgeAndDays() {
        val dob = LocalDate.of(1990, 5, 15)
        val patient = PatientDemographics(
            patientId = "PAT123",
            name = "John Doe",
            gender = Gender.MALE,
            bloodGroup = BloodGroup.O_POSITIVE,
            dateOfBirth = dob
        )
        val asOf = LocalDate.of(2025, 5, 15)
        assertEquals(35, patient.calculateAge(asOf))
        assertTrue(patient.daysSince1900() > 0)
    }

    @Test
    fun testBasicInfoDomainModel() {
        val basicInfo: BasicInfo = BasicInfo(
            patientId = "PT-555",
            name = "Sarah Connor",
            gender = Gender.FEMALE,
            bloodGroup = BloodGroup.A_POSITIVE,
            dateOfBirth = LocalDate.of(1984, 2, 28)
        )
        assertEquals("PT-555", basicInfo.patientId)
        assertEquals("Sarah Connor", basicInfo.name)
        assertEquals(Gender.FEMALE, basicInfo.gender)
        assertEquals(BloodGroup.A_POSITIVE, basicInfo.bloodGroup)
        assertEquals(LocalDate.of(1984, 2, 28), basicInfo.dateOfBirth)
        assertTrue(basicInfo.calculateAge(LocalDate.of(2026, 1, 1)) > 40)
    }

    @Test
    fun testClinicalRecordValidation() {
        val record = ClinicalRecord(
            timestamp = Instant.parse("2025-06-01T12:00:00Z"),
            hpId = "DOC01",
            vitalSigns = VitalSigns(heartRateBpm = 75),
            medications = listOf("Paracetamol 500mg"),
            description = "Routine vitals checkup"
        )
        assertNotNull(record)
        assertTrue(record.minutesSince2025Epoch() > 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testClinicalRecordNoDataThrows() {
        ClinicalRecord(
            timestamp = Instant.parse("2025-06-01T12:00:00Z"),
            hpId = "DOC01",
            vitalSigns = VitalSigns(),
            medications = emptyList(),
            description = null
        )
    }
}
