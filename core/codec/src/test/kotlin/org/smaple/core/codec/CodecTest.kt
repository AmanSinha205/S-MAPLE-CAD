package org.smaple.core.codec

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.smaple.core.codec.basicinfo.DefaultBasicInfoCodec
import org.smaple.core.codec.record.DefaultRecordCodec
import org.smaple.core.model.BloodGroup
import org.smaple.core.model.BloodPressure
import org.smaple.core.model.ClinicalRecord
import org.smaple.core.model.Gender
import org.smaple.core.model.PatientDemographics
import org.smaple.core.model.VitalSigns
import java.time.Instant
import java.time.LocalDate

class CodecTest {

    private val basicInfoCodec = DefaultBasicInfoCodec()
    private val recordCodec = DefaultRecordCodec()

    @Test
    fun testBasicInfoEncodeDecodeRoundTrip() {
        val original = PatientDemographics(
            patientId = "PAT999",
            name = "Jane Smith",
            gender = Gender.FEMALE,
            bloodGroup = BloodGroup.O_POSITIVE,
            dateOfBirth = LocalDate.of(1985, 3, 22)
        )

        val encoded = basicInfoCodec.encode(original)
        assertTrue(encoded.isNotEmpty())
        assertTrue(encoded.size <= DefaultBasicInfoCodec.MAX_BASIC_INFO_BYTES)

        val decodedResult = basicInfoCodec.decode(encoded)
        assertTrue(decodedResult is CodecResult.Success)
        val decoded = (decodedResult as CodecResult.Success).value

        assertEquals(original.patientId, decoded.patientId)
        assertEquals(original.name, decoded.name)
        assertEquals(original.gender, decoded.gender)
        assertEquals(original.bloodGroup, decoded.bloodGroup)
        assertEquals(original.dateOfBirth, decoded.dateOfBirth)
    }

    @Test
    fun testClinicalRecordEncodeDecodeRoundTrip() {
        val original = ClinicalRecord(
            timestamp = Instant.parse("2025-07-15T08:30:00Z"),
            hpId = "DOC77",
            vitalSigns = VitalSigns(
                bloodPressure = BloodPressure(systolic = 125, diastolic = 82),
                temperatureCelsius = 36.8f,
                heartRateBpm = 68
            ),
            medications = listOf("Amoxicillin 500mg", "Paracetamol 650mg"),
            description = "Patient recovering well"
        )

        val encoded = recordCodec.encode(original)
        assertTrue(encoded.isNotEmpty())
        assertTrue(encoded.size <= DefaultRecordCodec.MAX_RECORD_BYTES)

        val decodedResult = recordCodec.decode(encoded)
        assertTrue(decodedResult is CodecResult.Success)
        val decoded = (decodedResult as CodecResult.Success).value

        assertEquals(original.hpId, decoded.hpId)
        assertEquals(original.timestamp, decoded.timestamp)
        assertEquals(original.vitalSigns.bloodPressure?.systolic, decoded.vitalSigns.bloodPressure?.systolic)
        assertEquals(original.vitalSigns.bloodPressure?.diastolic, decoded.vitalSigns.bloodPressure?.diastolic)
        assertEquals(original.vitalSigns.heartRateBpm, decoded.vitalSigns.heartRateBpm)
        assertEquals(original.medications, decoded.medications)
        assertEquals(original.description, decoded.description)
    }

    @Test
    fun testDecodeInvalidBytesReturnsError() {
        val invalidBytes = byteArrayOf(0x00, 0x01, 0x02)
        val basicResult = basicInfoCodec.decode(invalidBytes)
        assertTrue(basicResult is CodecResult.Error)

        val recordResult = recordCodec.decode(invalidBytes)
        assertTrue(recordResult is CodecResult.Error)
    }
}
