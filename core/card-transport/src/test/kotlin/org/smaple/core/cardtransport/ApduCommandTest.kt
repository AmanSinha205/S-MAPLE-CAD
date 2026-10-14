package org.smaple.core.cardtransport

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ApduCommandTest {

    @Test
    fun testGetBasicInfoApduSerialization() {
        val cmd = CardCommands.getBasicInfo()
        val bytes = cmd.toByteArray()

        // Per API.md section 8: GET_BASIC_INFO: 80 02 00 00 00
        val expected = byteArrayOf(0x80.toByte(), 0x02, 0x00, 0x00, 0x00)
        assertArrayEquals(expected, bytes)
    }

    @Test
    fun testSelectApplicationApduSerialization() {
        val cmd = CardCommands.selectApplication()
        val bytes = cmd.toByteArray()

        // 00 A4 04 00 06 F0 12 34 56 78 21
        val expected = byteArrayOf(
            0x00, 0xA4.toByte(), 0x04, 0x00, 0x06,
            0xF0.toByte(), 0x12, 0x34, 0x56, 0x78, 0x21
        )
        assertArrayEquals(expected, bytes)
    }

    @Test
    fun testGetInfoApduSerialization() {
        val cmd = CardCommands.getInfo()
        val bytes = cmd.toByteArray()

        // Per API.md section 8: GET_INFO: 80 00 00 00 00
        val expected = byteArrayOf(0x80.toByte(), 0x00, 0x00, 0x00, 0x00)
        assertArrayEquals(expected, bytes)
    }

    @Test
    fun testGetRecordsApduSerialization() {
        // start index 0: 80 12 00 00 00
        val cmd0 = CardCommands.getRecords(0)
        assertArrayEquals(byteArrayOf(0x80.toByte(), 0x12, 0x00, 0x00, 0x00), cmd0.toByteArray())

        // start index 2: 80 12 00 02 00
        val cmd2 = CardCommands.getRecords(2)
        assertArrayEquals(byteArrayOf(0x80.toByte(), 0x12, 0x00, 0x02, 0x00), cmd2.toByteArray())
    }

    @Test
    fun testResponseApduParsingSuccess() {
        // Example response with basic info block followed by 90 00
        val rawResponse = byteArrayOf(0x10, 0x4C, 0x67.toByte(), 0x45, 0x90.toByte(), 0x00)
        val response = ResponseApdu.fromBytes(rawResponse)

        assertEquals(0x9000, response.sw)
        assertTrue(response.isSuccess)
        assertEquals(4, response.data.size)
        assertArrayEquals(byteArrayOf(0x10, 0x4C, 0x67.toByte(), 0x45), response.data)
    }

    @Test
    fun testResponseApduParsingError() {
        // Status word 6A88 (Data not found)
        val rawResponse = byteArrayOf(0x6A.toByte(), 0x88.toByte())
        val response = ResponseApdu.fromBytes(rawResponse)

        assertEquals(0x6A88, response.sw)
        assertFalse(response.isSuccess)
        assertEquals(0, response.data.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testResponseApduTooShortThrows() {
        ResponseApdu.fromBytes(byteArrayOf(0x90.toByte()))
    }

    @Test
    fun testCommandApduFromBytes() {
        // SELECT S-MAPLE AID
        val selectBytes = byteArrayOf(
            0x00, 0xA4.toByte(), 0x04, 0x00, 0x06,
            0xF0.toByte(), 0x12, 0x34, 0x56, 0x78, 0x21
        )
        val cmd = CommandApdu.fromBytes(selectBytes)
        assertEquals(0x00.toByte(), cmd.cla)
        assertEquals(0xA4.toByte(), cmd.ins)
        assertEquals(0x04.toByte(), cmd.p1)
        assertEquals(0x00.toByte(), cmd.p2)
        assertArrayEquals(CardCommands.SMAPLE_AID, cmd.data)

        // GET_BASIC_INFO: 80 02 00 00 00
        val getBasicInfoBytes = byteArrayOf(0x80.toByte(), 0x02, 0x00, 0x00, 0x00)
        val getCmd = CommandApdu.fromBytes(getBasicInfoBytes)
        assertEquals(0x80.toByte(), getCmd.cla)
        assertEquals(0x02.toByte(), getCmd.ins)
        assertEquals(0x00.toByte(), getCmd.le)
    }

    @Test
    fun testResponseApduToByteArray() {
        val success = ResponseApdu.success()
        assertArrayEquals(byteArrayOf(0x90.toByte(), 0x00), success.toByteArray())

        val withData = ResponseApdu.success(byteArrayOf(0x01, 0x02))
        assertArrayEquals(byteArrayOf(0x01, 0x02, 0x90.toByte(), 0x00), withData.toByteArray())

        val error = ResponseApdu.error(ApduConstants.SW_INS_NOT_SUPPORTED)
        assertArrayEquals(byteArrayOf(0x6D.toByte(), 0x00), error.toByteArray())
    }

    // =========================================================================
    // APDU Wire-Byte Round-Trip Compatibility Tests
    //
    // These tests verify that:
    //   CAD → CardCommands.buildX().toByteArray() == exact bytes mCard receives
    //   mCard → CommandApdu.fromBytes(received) reconstructs identical fields
    //
    // This is the definitive PASS/FAIL criterion for "will the tap actually work".
    // =========================================================================

    /**
     * SELECT S-MAPLE AID round-trip:
     * CAD builds 00 A4 04 00 06 F0 12 34 56 78 21 → mCard parses it back → fields match.
     */
    @Test
    fun `WIRE_COMPAT SELECT_AID - CAD bytes round-trip through mCard parser`() {
        // CAD side: build SELECT command
        val cadCommand = CardCommands.selectApplication(CardCommands.SMAPLE_AID)
        val wireBytes = cadCommand.toByteArray()

        // Assert exact wire bytes (regression guard)
        assertArrayEquals(
            "SELECT AID wire bytes must match spec: 00 A4 04 00 06 F0 12 34 56 78 21",
            byteArrayOf(0x00, 0xA4.toByte(), 0x04, 0x00, 0x06,
                0xF0.toByte(), 0x12, 0x34, 0x56, 0x78, 0x21),
            wireBytes
        )
        assertEquals("SELECT AID APDU must be exactly 11 bytes", 11, wireBytes.size)

        // mCard side: parse the bytes received over NFC
        val mCardParsed = CommandApdu.fromBytes(wireBytes)
        assertEquals("CLA must be 00", 0x00.toByte(), mCardParsed.cla)
        assertEquals("INS must be A4 (SELECT)", 0xA4.toByte(), mCardParsed.ins)
        assertEquals("P1 must be 04 (AID selection)", 0x04.toByte(), mCardParsed.p1)
        assertEquals("P2 must be 00", 0x00.toByte(), mCardParsed.p2)
        assertNotNull("AID data must not be null", mCardParsed.data)
        assertArrayEquals("AID data must match S-MAPLE AID F01234567821",
            CardCommands.SMAPLE_AID, mCardParsed.data)
    }

    /**
     * GET_BASIC_INFO round-trip:
     * CAD builds 80 02 00 00 00 → mCard parses it back → INS=0x02, P1=P2=0x00.
     */
    @Test
    fun `WIRE_COMPAT GET_BASIC_INFO - CAD bytes round-trip through mCard parser`() {
        // CAD side: build GET_BASIC_INFO command
        val cadCommand = CardCommands.getBasicInfo()
        val wireBytes = cadCommand.toByteArray()

        // Assert exact wire bytes (regression guard)
        assertArrayEquals(
            "GET_BASIC_INFO wire bytes must match spec: 80 02 00 00 00",
            byteArrayOf(0x80.toByte(), 0x02, 0x00, 0x00, 0x00),
            wireBytes
        )
        assertEquals("GET_BASIC_INFO APDU must be exactly 5 bytes", 5, wireBytes.size)

        // mCard side: parse the bytes received over NFC
        val mCardParsed = CommandApdu.fromBytes(wireBytes)
        assertEquals("CLA must be 80", ApduConstants.CLA, mCardParsed.cla)
        assertEquals("INS must be 02 (GET_BASIC_INFO)", ApduConstants.INS_GET_BASIC_INFO, mCardParsed.ins)
        assertEquals("P1 must be 00", 0x00.toByte(), mCardParsed.p1)
        assertEquals("P2 must be 00", 0x00.toByte(), mCardParsed.p2)
        assertEquals("Le must be 00", 0x00.toByte(), mCardParsed.le)
    }

    /**
     * SELECT AID 9000 response round-trip:
     * mCard calls ResponseApdu.success() → CAD receives raw bytes → parsed SW == 9000.
     */
    @Test
    fun `WIRE_COMPAT SELECT_AID success response - mCard bytes round-trip through CAD parser`() {
        // mCard side: build 90 00 success response
        val mCardResponse = ResponseApdu.success()
        val wireBytes = mCardResponse.toByteArray()

        assertArrayEquals("SELECT success response must be exactly 90 00",
            byteArrayOf(0x90.toByte(), 0x00), wireBytes)

        // CAD side: parse raw bytes from NFC transceive
        val cadParsed = ResponseApdu.fromBytes(wireBytes)
        assertTrue("CAD must interpret 90 00 as success", cadParsed.isSuccess)
        assertEquals("SW must be 9000", 0x9000, cadParsed.sw)
        assertEquals("No data payload for SELECT response", 0, cadParsed.data.size)
    }

    /**
     * GET_BASIC_INFO response round-trip:
     * mCard encodes sample data + 90 00 → CAD parses bytes → data + isSuccess preserved.
     */
    @Test
    fun `WIRE_COMPAT GET_BASIC_INFO response - mCard payload bytes round-trip through CAD parser`() {
        // Simulated BasicInfo payload bytes (26-byte sample as per codec format)
        val samplePayload = byteArrayOf(
            0x01,                                // version=1
            0x00, 0x07,                          // patientId length=7
            0x41, 0x56, 0x2D, 0x32, 0x30, 0x34, 0x31,  // "AV-2041"
            0x00, 0x0A,                          // name length=10
            0x41, 0x73, 0x68, 0x61, 0x20, 0x56, 0x65, 0x72, 0x6D, 0x61,  // "Asha Verma"
            0x02,                                // gender=FEMALE
            0x05,                                // bloodGroup=B+
            0x67.toByte(), 0x45, 0x00, 0x00     // dateOfBirth placeholder
        )

        // mCard side: build success response with payload
        val mCardResponse = ResponseApdu.success(samplePayload)
        val wireBytes = mCardResponse.toByteArray()

        // Wire format: [payload bytes] [90] [00]
        assertEquals("Wire response must be payloadLen+2", samplePayload.size + 2, wireBytes.size)
        assertEquals("Last byte must be 00 (SW2)", 0x00.toByte(), wireBytes.last())
        assertEquals("Second-to-last byte must be 90 (SW1)", 0x90.toByte(), wireBytes[wireBytes.size - 2])

        // CAD side: parse raw bytes from NFC transceive
        val cadParsed = ResponseApdu.fromBytes(wireBytes)
        assertTrue("CAD must interpret as success", cadParsed.isSuccess)
        assertEquals("SW must be 9000", 0x9000, cadParsed.sw)
        assertArrayEquals("Data payload must be preserved verbatim", samplePayload, cadParsed.data)
    }

    /**
     * All documented error status words round-trip correctly.
     */
    @Test
    fun `WIRE_COMPAT all error status words round-trip`() {
        val statusWords = listOf(
            ApduConstants.SW_WRONG_LENGTH to "6700",
            ApduConstants.SW_NO_SECURE_SESSION to "6982",
            ApduConstants.SW_CONDITIONS_NOT_SATISFIED to "6985",
            ApduConstants.SW_INCORRECT_DATA to "6A80",
            ApduConstants.SW_WRONG_P1P2 to "6A86",
            ApduConstants.SW_DATA_NOT_FOUND to "6A88",
            ApduConstants.SW_INS_NOT_SUPPORTED to "6D00",
            ApduConstants.SW_CLA_NOT_SUPPORTED to "6E00",
        )
        for ((sw, label) in statusWords) {
            val errorResponse = ResponseApdu.error(sw)
            val wireBytes = errorResponse.toByteArray()
            val parsed = ResponseApdu.fromBytes(wireBytes)
            assertFalse("$label must not be success", parsed.isSuccess)
            assertEquals("$label SW must round-trip", sw, parsed.sw)
        }
    }
}
