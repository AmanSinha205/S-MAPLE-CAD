package org.smaple.mcard

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.smaple.core.cardtransport.ApduConstants
import org.smaple.core.cardtransport.CardCommands
import org.smaple.core.cardtransport.ResponseApdu
import org.smaple.core.codec.CodecResult
import org.smaple.core.codec.basicinfo.DefaultBasicInfoCodec
import org.smaple.core.model.BloodGroup
import org.smaple.core.model.Gender
import org.smaple.mcard.data.SampleBasicInfoProvider
import java.time.LocalDate

class MCardHostApduServiceTest {

    private lateinit var service: MCardHostApduService
    private val basicInfoCodec = DefaultBasicInfoCodec()
    private val sampleProvider = SampleBasicInfoProvider()

    @Before
    fun setUp() {
        service = MCardHostApduService().apply {
            basicInfoProvider = sampleProvider
            this.basicInfoCodec = this@MCardHostApduServiceTest.basicInfoCodec
        }
    }

    @Test
    fun testSelectSmapleAidReturns9000() {
        val selectApdu = CardCommands.selectApplication().toByteArray()
        val response = service.processCommandApdu(selectApdu, null)

        assertArrayEquals(ResponseApdu.SUCCESS_STATUS_BYTES, response)
    }

    @Test
    fun testSelectWrongAidReturnsDataNotFound() {
        val wrongAid = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        val selectApdu = CardCommands.selectApplication(wrongAid).toByteArray()
        val response = service.processCommandApdu(selectApdu, null)

        val parsed = ResponseApdu.fromBytes(response)
        assertEquals(ApduConstants.SW_DATA_NOT_FOUND, parsed.sw)
    }

    @Test
    fun testCommandBeforeSelectReturnsConditionsNotSatisfied() {
        val getBasicInfoApdu = CardCommands.getBasicInfo().toByteArray()
        val response = service.processCommandApdu(getBasicInfoApdu, null)

        val parsed = ResponseApdu.fromBytes(response)
        assertEquals(ApduConstants.SW_CONDITIONS_NOT_SATISFIED, parsed.sw)
    }

    @Test
    fun testGetBasicInfoReturnsEncodedPayloadWith9000() {
        // 1. Select AID
        val selectApdu = CardCommands.selectApplication().toByteArray()
        service.processCommandApdu(selectApdu, null)

        // 2. Issue GET_BASIC_INFO
        val getBasicInfoApdu = CardCommands.getBasicInfo().toByteArray()
        val response = service.processCommandApdu(getBasicInfoApdu, null)

        val parsed = ResponseApdu.fromBytes(response)
        assertEquals(ApduConstants.SW_SUCCESS, parsed.sw)
        assertTrue(parsed.isSuccess)
        assertTrue(parsed.data.isNotEmpty())

        // 3. Decode response payload using BasicInfoCodec
        val decodeResult = basicInfoCodec.decode(parsed.data)
        assertTrue(decodeResult is CodecResult.Success)

        val basicInfo = (decodeResult as CodecResult.Success).value
        assertEquals("AV-2041", basicInfo.patientId)
        assertEquals("Asha Verma", basicInfo.name)
        assertEquals(Gender.FEMALE, basicInfo.gender)
        assertEquals(BloodGroup.B_POSITIVE, basicInfo.bloodGroup)
        assertEquals(LocalDate.of(1972, 5, 20), basicInfo.dateOfBirth)
    }

    @Test
    fun testUnsupportedInstructionReturns6D00() {
        // Select AID
        service.processCommandApdu(CardCommands.selectApplication().toByteArray(), null)

        // Command with unknown INS 0x99
        val unknownCommand = byteArrayOf(0x80.toByte(), 0x99.toByte(), 0x00, 0x00)
        val response = service.processCommandApdu(unknownCommand, null)

        val parsed = ResponseApdu.fromBytes(response)
        assertEquals(ApduConstants.SW_INS_NOT_SUPPORTED, parsed.sw)
    }

    @Test
    fun testUnsupportedClaReturns6E00() {
        // Select AID
        service.processCommandApdu(CardCommands.selectApplication().toByteArray(), null)

        // Command with invalid CLA 0x00 for GET_BASIC_INFO
        val invalidClaCommand = byteArrayOf(0x00.toByte(), 0x02, 0x00, 0x00)
        val response = service.processCommandApdu(invalidClaCommand, null)

        val parsed = ResponseApdu.fromBytes(response)
        assertEquals(ApduConstants.SW_CLA_NOT_SUPPORTED, parsed.sw)
    }

    @Test
    fun testOnDeactivatedClearsSelectionState() {
        // Select AID
        service.processCommandApdu(CardCommands.selectApplication().toByteArray(), null)

        // Deactivate (card moved away)
        service.onDeactivated(1)

        // Next command should fail with conditions not satisfied until re-selected
        val getBasicInfoApdu = CardCommands.getBasicInfo().toByteArray()
        val response = service.processCommandApdu(getBasicInfoApdu, null)

        val parsed = ResponseApdu.fromBytes(response)
        assertEquals(ApduConstants.SW_CONDITIONS_NOT_SATISFIED, parsed.sw)
    }
}
