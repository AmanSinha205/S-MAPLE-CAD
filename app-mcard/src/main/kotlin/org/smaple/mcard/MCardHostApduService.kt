package org.smaple.mcard

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import org.smaple.core.cardtransport.ApduConstants
import org.smaple.core.cardtransport.CardCommands
import org.smaple.core.cardtransport.CommandApdu
import org.smaple.core.cardtransport.NfcDebugConfig
import org.smaple.core.cardtransport.ResponseApdu
import org.smaple.core.codec.basicinfo.BasicInfoCodec
import org.smaple.core.codec.util.ByteBuffers
import org.smaple.mcard.data.BasicInfoProvider
import javax.inject.Inject


/**
 * Android Host Card Emulation (HCE) service simulating the S-MAPLE mCard smartcard.
 *
 * Implements ISO 7816-4 APDU handling:
 * - SELECT S-MAPLE AID (00 A4 04 00 06 F0 12 34 56 78 21) -> 90 00
 * - GET_BASIC_INFO (80 02 00 00 00) -> <BasicInfo bytes> 90 00
 */
@AndroidEntryPoint
class MCardHostApduService : HostApduService() {

    @Inject
    lateinit var basicInfoProvider: BasicInfoProvider

    @Inject
    lateinit var basicInfoCodec: BasicInfoCodec

    private var isAppletSelected: Boolean = false

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null || commandApdu.size < 4) {
            Log.w(TAG, "APDU received is null or shorter than 4 header bytes (len=${commandApdu?.size ?: 0})")
            val errorResponse = ResponseApdu.error(ApduConstants.SW_WRONG_LENGTH)
            return logAndReturn(errorResponse)
        }

        if (NfcDebugConfig.ENABLED && NfcDebugConfig.LOG_APDU_BYTES) {
            Log.d(TAG, "RX APDU: ${ByteBuffers.toHexString(commandApdu)} (${commandApdu.size} bytes)")
        } else {
            Log.i(TAG, "APDU received (${commandApdu.size} bytes) CLA=0x${commandApdu[0].toUByte().toString(16).uppercase()} " +
                    "INS=0x${commandApdu[1].toUByte().toString(16).uppercase()}")
        }

        val command = try {
            CommandApdu.fromBytes(commandApdu)
        } catch (e: Exception) {
            Log.e(TAG, "Malformed APDU — could not parse: ${e.message}")
            val errorResponse = ResponseApdu.error(ApduConstants.SW_INCORRECT_DATA)
            return logAndReturn(errorResponse)
        }


        // 1. SELECT S-MAPLE AID: 00 A4 04 00 <length> <AID>
        if (command.cla == 0x00.toByte() &&
            command.ins == 0xA4.toByte() &&
            command.p1 == 0x04.toByte() &&
            command.p2 == 0x00.toByte()
        ) {
            Log.i(TAG, "Command type: SELECT APPLICATION (AID)")
            val commandData = command.data
            return if (commandData != null && commandData.contentEquals(CardCommands.SMAPLE_AID)) {
                isAppletSelected = true
                Log.i(TAG, "S-MAPLE applet selected successfully (AID: ${ByteBuffers.toHexString(commandData)})")
                logAndReturn(ResponseApdu.success())
            } else {
                isAppletSelected = false
                val requestedAid = commandData?.let { ByteBuffers.toHexString(it) } ?: "none"
                Log.w(TAG, "SELECT AID rejected: target AID ($requestedAid) does not match S-MAPLE AID")
                logAndReturn(ResponseApdu.error(ApduConstants.SW_DATA_NOT_FOUND))
            }
        }

        // 2. Reject commands if applet has not been selected yet
        if (!isAppletSelected) {
            Log.w(TAG, "Command rejected: S-MAPLE applet has not been selected yet")
            val errorResponse = ResponseApdu.error(ApduConstants.SW_CONDITIONS_NOT_SATISFIED)
            return logAndReturn(errorResponse)
        }

        // 3. Validate CLA is 0x80 per S-MAPLE specification Section 1 & 2
        if (command.cla != ApduConstants.CLA) {
            Log.w(TAG, "Command rejected: Unsupported CLA 0x${command.cla.toUByte().toString(16).uppercase()}")
            val errorResponse = ResponseApdu.error(ApduConstants.SW_CLA_NOT_SUPPORTED)
            return logAndReturn(errorResponse)
        }

        // 4. Dispatch supported instructions
        return when (command.ins) {
            ApduConstants.INS_GET_BASIC_INFO -> {
                Log.i(TAG, "Command type: GET_BASIC_INFO (0x02)")
                // P1 and P2 must be 00 00
                if (command.p1 != 0x00.toByte() || command.p2 != 0x00.toByte()) {
                    Log.w(TAG, "GET_BASIC_INFO rejected: non-zero P1/P2 (P1=0x${command.p1.toUByte().toString(16)}, P2=0x${command.p2.toUByte().toString(16)})")
                    val errorResponse = ResponseApdu.error(ApduConstants.SW_WRONG_P1P2)
                    return logAndReturn(errorResponse)
                }

                val basicInfo = basicInfoProvider.getBasicInfo()
                if (basicInfo == null) {
                    Log.w(TAG, "GET_BASIC_INFO failed: No patient BasicInfo record found")
                    val errorResponse = ResponseApdu.error(ApduConstants.SW_DATA_NOT_FOUND)
                    return logAndReturn(errorResponse)
                }

                val encodedBytes = basicInfoCodec.encode(basicInfo)
                Log.i(
                    TAG,
                    "Encoded BasicInfo for patient '${basicInfo.name}' (${encodedBytes.size} bytes: ${ByteBuffers.toHexString(encodedBytes)})"
                )

                val response = ResponseApdu.success(encodedBytes)
                logAndReturn(response)
            }

            else -> {
                Log.w(TAG, "Command rejected: Unsupported INS 0x${command.ins.toUByte().toString(16).uppercase()}")
                val errorResponse = ResponseApdu.error(ApduConstants.SW_INS_NOT_SUPPORTED)
                logAndReturn(errorResponse)
            }
        }
    }

    override fun onDeactivated(reason: Int) {
        val reasonStr = when (reason) {
            DEACTIVATION_LINK_LOSS -> "LINK_LOSS"
            DEACTIVATION_DESELECTED -> "DESELECTED"
            else -> "UNKNOWN ($reason)"
        }
        Log.i(TAG, "HCE contactless session deactivated: $reasonStr")
        isAppletSelected = false
    }

    private fun logAndReturn(response: ResponseApdu): ByteArray {
        val bytes = response.toByteArray()
        val swHex = "0x${response.sw.toString(16).uppercase().padStart(4, '0')}"
        if (NfcDebugConfig.ENABLED && NfcDebugConfig.LOG_RESPONSE_BYTES) {
            Log.d(TAG, "TX APDU response: SW=$swHex DataLen=${response.data.size} total=${bytes.size} bytes " +
                    "(${ByteBuffers.toHexString(bytes)})")
        } else {
            Log.i(TAG, "TX APDU response: SW=$swHex DataLen=${response.data.size} bytes")
        }
        return bytes
    }


    companion object {
        private const val TAG = "SMaple-mCard"
    }
}
