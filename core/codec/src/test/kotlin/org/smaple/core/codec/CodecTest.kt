package org.smaple.core.codec

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.smaple.core.codec.basicinfo.DefaultBasicInfoCodec
import org.smaple.core.codec.record.DefaultRecordCodec
import org.smaple.core.codec.util.ByteBuffers
import org.smaple.core.model.BasicInfo
import org.smaple.core.model.BloodGroup
import org.smaple.core.model.BloodPressure
import org.smaple.core.model.ClinicalRecord
import org.smaple.core.model.Gender
import org.smaple.core.model.PatientDemographics
import org.smaple.core.model.VitalSigns
import java.time.Instant
import java.time.LocalDate

/**
 * Unit tests verifying codec compliance with S-MAPLE card command specification (v1) section 8.
 * All test vectors are verified byte-for-byte against the reference vectors.
 */
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
    fun testSpecBasicInfoAshaVerma() {
        // Asha Verma, female, B+, born 1972-05-20 (26,437 days), patient ID AV-2041 (23 bytes)
        // 10 4C 67 45 07 41 56 2D 32 30 34 31 0A 41 73 68 61 20 56 65 72 6D 61
        val expectedHex = "104C67450741562D323034310A41736861205665726D61"
        val expectedBytes = ByteBuffers.fromHexString(expectedHex)

        val asha = PatientDemographics(
            patientId = "AV-2041",
            name = "Asha Verma",
            gender = Gender.FEMALE,
            bloodGroup = BloodGroup.B_POSITIVE,
            dateOfBirth = LocalDate.of(1972, 5, 20)
        )

        val encoded = basicInfoCodec.encode(asha)
        assertEquals(expectedHex, ByteBuffers.toHexString(encoded))
        assertArrayEquals(expectedBytes, encoded)
        assertEquals(23, encoded.size)

        val decodedResult = basicInfoCodec.decode(expectedBytes)
        assertTrue(decodedResult is CodecResult.Success)
        val decoded = (decodedResult as CodecResult.Success).value

        assertEquals("AV-2041", decoded.patientId)
        assertEquals("Asha Verma", decoded.name)
        assertEquals(Gender.FEMALE, decoded.gender)
        assertEquals(BloodGroup.B_POSITIVE, decoded.bloodGroup)
        assertEquals(LocalDate.of(1972, 5, 20), decoded.dateOfBirth)
        assertEquals(26437, decoded.daysSince1900())
    }

    @Test
    fun testSpecRecordV1() {
        // V1, 2026-10-11 10:20 UTC, HP N101, BP 120/80, 37.0 C, heart rate 80, description "Fever 2 days" (26 bytes).
        // Timestamp is 933740 minutes (0E 3F 6C).
        // 3D 0E 3F 6C 04 4E 31 30 31 50 3C 46 50 0C 46 65 76 65 72 20 32 20 64 61 79 73
        val expectedHex = "3D0E3F6C044E313031503C46500C466576657220322064617973"
        val expectedBytes = ByteBuffers.fromHexString(expectedHex)

        val v1 = ClinicalRecord(
            timestamp = Instant.parse("2026-10-11T10:20:00Z"),
            hpId = "N101",
            vitalSigns = VitalSigns(
                bloodPressure = BloodPressure(systolic = 120, diastolic = 80),
                temperatureCelsius = 37.0f,
                heartRateBpm = 80
            ),
            description = "Fever 2 days"
        )

        val encoded = recordCodec.encode(v1)
        assertEquals(expectedHex, ByteBuffers.toHexString(encoded))
        assertArrayEquals(expectedBytes, encoded)
        assertEquals(26, encoded.size)

        val decodedResult = recordCodec.decode(expectedBytes)
        assertTrue(decodedResult is CodecResult.Success)
        val decoded = (decodedResult as CodecResult.Success).value

        assertEquals(v1.hpId, decoded.hpId)
        assertEquals(v1.timestamp, decoded.timestamp)
        assertEquals(120, decoded.vitalSigns.bloodPressure?.systolic)
        assertEquals(80, decoded.vitalSigns.bloodPressure?.diastolic)
        assertEquals(37.0f, decoded.vitalSigns.temperatureCelsius!!, 0.01f)
        assertEquals(80, decoded.vitalSigns.heartRateBpm)
        assertTrue(decoded.medications.isEmpty())
        assertEquals("Fever 2 days", decoded.description)
    }

    @Test
    fun testSpecRecordV2() {
        // V2, 2026-10-11 09:45 UTC, HP N101, heart rate 72 only (10 bytes):
        // 24 0E 3F 49 04 4E 31 30 31 48
        val expectedHex = "240E3F49044E31303148"
        val expectedBytes = ByteBuffers.fromHexString(expectedHex)

        val v2 = ClinicalRecord(
            timestamp = Instant.parse("2026-10-11T09:45:00Z"),
            hpId = "N101",
            vitalSigns = VitalSigns(
                heartRateBpm = 72
            )
        )

        val encoded = recordCodec.encode(v2)
        assertEquals(expectedHex, ByteBuffers.toHexString(encoded))
        assertArrayEquals(expectedBytes, encoded)
        assertEquals(10, encoded.size)

        val decodedResult = recordCodec.decode(expectedBytes)
        assertTrue(decodedResult is CodecResult.Success)
        val decoded = (decodedResult as CodecResult.Success).value

        assertEquals(v2.hpId, decoded.hpId)
        assertEquals(v2.timestamp, decoded.timestamp)
        assertEquals(null, decoded.vitalSigns.bloodPressure)
        assertEquals(null, decoded.vitalSigns.temperatureCelsius)
        assertEquals(72, decoded.vitalSigns.heartRateBpm)
    }

    @Test
    fun testSpecRecordV3() {
        // V3, 2026-10-10 16:05 UTC, HP N102, BP 128/84, 36.9 C, heart rate 76,
        // medications "Paracetamol 500 mg" and "ORS", description "Cough, advised rest. Review in 3 days" (74 bytes)
        // 3F 0E 3B 25 04 4E 31 30 32 58 40 45 4C 16 50 61 72 61 63 65 74 61 6D 6F 6C 20 35 30 30 20 6D 67 3B 4F 52 53 25 43 6F 75 67 68 2C 20 61 64 76 69 73 65 64 20 72 65 73 74 2E 20 52 65 76 69 65 77 20 69 6E 20 33 20 64 61 79 73
        val expectedHex = "3F0E3B25044E3130325840454C1650617261636574616D6F6C20353030206D673B4F525325436F7567682C206164766973656420726573742E2052657669657720696E20332064617973"
        val expectedBytes = ByteBuffers.fromHexString(expectedHex)

        val v3 = ClinicalRecord(
            timestamp = Instant.parse("2026-10-10T16:05:00Z"),
            hpId = "N102",
            vitalSigns = VitalSigns(
                bloodPressure = BloodPressure(systolic = 128, diastolic = 84),
                temperatureCelsius = 36.9f,
                heartRateBpm = 76
            ),
            medications = listOf("Paracetamol 500 mg", "ORS"),
            description = "Cough, advised rest. Review in 3 days"
        )

        val encoded = recordCodec.encode(v3)
        assertEquals(expectedHex, ByteBuffers.toHexString(encoded))
        assertArrayEquals(expectedBytes, encoded)
        assertEquals(74, encoded.size)

        val decodedResult = recordCodec.decode(expectedBytes)
        assertTrue(decodedResult is CodecResult.Success)
        val decoded = (decodedResult as CodecResult.Success).value

        assertEquals(v3.hpId, decoded.hpId)
        assertEquals(v3.timestamp, decoded.timestamp)
        assertEquals(128, decoded.vitalSigns.bloodPressure?.systolic)
        assertEquals(84, decoded.vitalSigns.bloodPressure?.diastolic)
        assertEquals(36.9f, decoded.vitalSigns.temperatureCelsius!!, 0.01f)
        assertEquals(76, decoded.vitalSigns.heartRateBpm)
        assertEquals(listOf("Paracetamol 500 mg", "ORS"), decoded.medications)
        assertEquals("Cough, advised rest. Review in 3 days", decoded.description)
    }

    @Test
    fun testSpecRecordV4MaxPayload() {
        // V4 (maximum, 179 bytes), 2026-01-01 00:00 UTC, HP N1010101, BP 295/275, 55.5 C, heart rate 255,
        // medications 60 times m, description 100 times d.
        // First 12 bytes: 3F 08 05 20 08 4E 31 30 31 30 31 30
        val v4 = ClinicalRecord(
            timestamp = Instant.parse("2026-01-01T00:00:00Z"),
            hpId = "N1010101",
            vitalSigns = VitalSigns(
                bloodPressure = BloodPressure(systolic = 295, diastolic = 275),
                temperatureCelsius = 55.5f,
                heartRateBpm = 255
            ),
            medications = listOf("m".repeat(60)),
            description = "d".repeat(100)
        )

        val encoded = recordCodec.encode(v4)
        assertEquals(179, encoded.size)
        assertEquals("3F080520084E313031303130", ByteBuffers.toHexString(encoded.sliceArray(0 until 12)))

        val decodedResult = recordCodec.decode(encoded)
        assertTrue(decodedResult is CodecResult.Success)
        val decoded = (decodedResult as CodecResult.Success).value

        assertEquals("N1010101", decoded.hpId)
        assertEquals(v4.timestamp, decoded.timestamp)
        assertEquals(295, decoded.vitalSigns.bloodPressure?.systolic)
        assertEquals(275, decoded.vitalSigns.bloodPressure?.diastolic)
        assertEquals(55.5f, decoded.vitalSigns.temperatureCelsius!!, 0.01f)
        assertEquals(255, decoded.vitalSigns.heartRateBpm)
        assertEquals(listOf("m".repeat(60)), decoded.medications)
        assertEquals("d".repeat(100), decoded.description)
    }

    @Test
    fun testDecodeInvalidBytesReturnsError() {
        val invalidBytes = byteArrayOf(0x00, 0x01, 0x02)
        val basicResult = basicInfoCodec.decode(invalidBytes)
        assertTrue(basicResult is CodecResult.Error)

        val recordResult = recordCodec.decode(invalidBytes)
        assertTrue(recordResult is CodecResult.Error)
    }

    @Test
    fun testBasicInfoRejectsBadVersionOrReservedBits() {
        // Bad version byte: 0x20 instead of 0x10
        val badVersion = ByteBuffers.fromHexString("204C67450741562D323034310A41736861205665726D61")
        val res1 = basicInfoCodec.decode(badVersion)
        assertTrue(res1 is CodecResult.Error)

        // Non-zero reserved bits in byte 1 (0x4D has bit 0 set)
        val badReserved = ByteBuffers.fromHexString("104D67450741562D323034310A41736861205665726D61")
        val res2 = basicInfoCodec.decode(badReserved)
        assertTrue(res2 is CodecResult.Error)
    }

    @Test
    fun testRecordRejectsBadVersionOrZeroFlags() {
        // Header with version 2 instead of 1 (0x40 instead of 0x20)
        val badVersion = ByteBuffers.fromHexString("4D0E3F6C044E313031503C46500C466576657220322064617973")
        val res1 = recordCodec.decode(badVersion)
        assertTrue(res1 is CodecResult.Error)

        // Header with 0 presence flags (0x20)
        val noFlags = ByteBuffers.fromHexString("200E3F6C044E313031")
        val res2 = recordCodec.decode(noFlags)
        assertTrue(res2 is CodecResult.Error)
    }

    @Test
    fun testBasicInfoDecodeWithSuccessStatusWord() {
        // Spec vector for Asha Verma with trailing 90 00 (response APDU format)
        val responseHex = "104C67450741562D323034310A41736861205665726D619000"
        val responseBytes = ByteBuffers.fromHexString(responseHex)

        val result = basicInfoCodec.decode(responseBytes)
        assertTrue(result is CodecResult.Success)
        val decoded = (result as CodecResult.Success).value
        assertEquals("AV-2041", decoded.patientId)
        assertEquals("Asha Verma", decoded.name)
        assertEquals(Gender.FEMALE, decoded.gender)
        assertEquals(BloodGroup.B_POSITIVE, decoded.bloodGroup)
    }

    @Test
    fun testBasicInfoDecodeCardErrorStatusWords() {
        // SW 6A88 (Data not found / Basic info not written)
        val dataNotFound = ByteBuffers.fromHexString("6A88")
        val result1 = basicInfoCodec.decode(dataNotFound)
        assertTrue(result1 is CodecResult.Error)
        assertTrue((result1 as CodecResult.Error).message.contains("6A88"))

        // SW 6982 (No secure session)
        val noSession = ByteBuffers.fromHexString("6982")
        val result2 = basicInfoCodec.decode(noSession)
        assertTrue(result2 is CodecResult.Error)
        assertTrue((result2 as CodecResult.Error).message.contains("6982"))
    }

    @Test
    fun testBasicInfoDecodeRejectsTrailingGarbage() {
        // Asha Verma block + 2 extra garbage bytes (not 9000)
        val garbageHex = "104C67450741562D323034310A41736861205665726D61FFFF"
        val result = basicInfoCodec.decode(ByteBuffers.fromHexString(garbageHex))
        assertTrue(result is CodecResult.Error)
        assertTrue((result as CodecResult.Error).message.contains("Unexpected trailing bytes"))
    }

    @Test
    fun testBasicInfoDecodeEmptyReturnsError() {
        val result = basicInfoCodec.decode(byteArrayOf())
        assertTrue(result is CodecResult.Error)
    }
}
