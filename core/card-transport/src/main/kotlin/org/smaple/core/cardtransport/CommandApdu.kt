package org.smaple.core.cardtransport

import java.io.ByteArrayOutputStream

/**
 * Standard constants from S-MAPLE card command specification (v1).
 */
object ApduConstants {
    /**
     * Standard S-MAPLE Class byte (Section 1).
     */
    const val CLA: Byte = 0x80.toByte()

    // Instruction bytes (Section 2)
    const val INS_GET_INFO: Byte = 0x00.toByte()
    const val INS_GET_BASIC_INFO: Byte = 0x02.toByte()
    const val INS_ADD_RECORD: Byte = 0x10.toByte()
    const val INS_GET_RECORDS: Byte = 0x12.toByte()
    const val INS_DEV_WRITE_BASIC_INFO: Byte = 0xE0.toByte()

    // Status Words (Section 5)
    const val SW_SUCCESS: Int = 0x9000
    const val SW_WRONG_LENGTH: Int = 0x6700
    const val SW_NO_SECURE_SESSION: Int = 0x6982
    const val SW_CONDITIONS_NOT_SATISFIED: Int = 0x6985
    const val SW_INCORRECT_DATA: Int = 0x6A80
    const val SW_WRONG_P1P2: Int = 0x6A86
    const val SW_DATA_NOT_FOUND: Int = 0x6A88
    const val SW_INS_NOT_SUPPORTED: Int = 0x6D00
    const val SW_CLA_NOT_SUPPORTED: Int = 0x6E00

    // Size constraints (Section 1)
    const val MAX_COMMAND_DATA_BYTES = 180
    const val MAX_RESPONSE_DATA_BYTES = 200
}

/**
 * Represents an ISO 7816-4 short APDU command for S-MAPLE contactless card communication.
 */
data class CommandApdu(
    val cla: Byte = ApduConstants.CLA,
    val ins: Byte,
    val p1: Byte = 0x00,
    val p2: Byte = 0x00,
    val data: ByteArray? = null,
    val le: Byte? = null
) {
    init {
        data?.let {
            require(it.size in 1..ApduConstants.MAX_COMMAND_DATA_BYTES) {
                "Command data length must be between 1 and ${ApduConstants.MAX_COMMAND_DATA_BYTES} bytes (got ${it.size})"
            }
        }
    }

    /**
     * Serializes this command into the standard APDU byte array:
     * [CLA, INS, P1, P2, (Lc, Data)?, (Le)?]
     */
    fun toByteArray(): ByteArray {
        val stream = ByteArrayOutputStream()
        stream.write(cla.toInt() and 0xFF)
        stream.write(ins.toInt() and 0xFF)
        stream.write(p1.toInt() and 0xFF)
        stream.write(p2.toInt() and 0xFF)

        if (data != null && data.isNotEmpty()) {
            stream.write(data.size and 0xFF) // Lc
            stream.write(data)
        }

        if (le != null) {
            stream.write(le.toInt() and 0xFF) // Le
        }

        return stream.toByteArray()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CommandApdu
        if (cla != other.cla) return false
        if (ins != other.ins) return false
        if (p1 != other.p1) return false
        if (p2 != other.p2) return false
        if (data != null) {
            if (other.data == null) return false
            if (!data.contentEquals(other.data)) return false
        } else if (other.data != null) return false
        if (le != other.le) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cla.toInt()
        result = 31 * result + ins.toInt()
        result = 31 * result + p1.toInt()
        result = 31 * result + p2.toInt()
        result = 31 * result + (data?.contentHashCode() ?: 0)
        result = 31 * result + (le?.toInt() ?: 0)
        return result
    }

    companion object {
        /**
         * Parses a raw byte array into a [CommandApdu].
         * Supports ISO 7816-4 short APDUs (Case 1, 2, 3, 4).
         */
        fun fromBytes(bytes: ByteArray): CommandApdu {
            require(bytes.size >= 4) {
                "Command APDU must contain at least 4 header bytes (CLA, INS, P1, P2), got ${bytes.size}"
            }
            val cla = bytes[0]
            val ins = bytes[1]
            val p1 = bytes[2]
            val p2 = bytes[3]

            if (bytes.size == 4) {
                return CommandApdu(cla, ins, p1, p2)
            }
            if (bytes.size == 5) {
                return CommandApdu(cla, ins, p1, p2, le = bytes[4])
            }

            val lc = bytes[4].toInt() and 0xFF
            val dataEnd = 5 + lc
            require(bytes.size >= dataEnd) {
                "Malformed APDU: Lc specifies $lc data bytes, but buffer only has ${bytes.size - 5} bytes"
            }
            val data = bytes.copyOfRange(5, dataEnd)
            val le = if (bytes.size > dataEnd) bytes[dataEnd] else null
            return CommandApdu(cla, ins, p1, p2, data, le)
        }
    }
}

/**
 * Parsed representation of an ISO 7816-4 response APDU:
 * Response body bytes followed by status word SW1-SW2.
 */
data class ResponseApdu(
    val data: ByteArray,
    val sw1: Byte,
    val sw2: Byte
) {
    val sw: Int
        get() = ((sw1.toInt() and 0xFF) shl 8) or (sw2.toInt() and 0xFF)

    val isSuccess: Boolean
        get() = sw == ApduConstants.SW_SUCCESS

    /**
     * Serializes this response APDU into raw bytes: data bytes + SW1 + SW2.
     */
    fun toByteArray(): ByteArray {
        val result = ByteArray(data.size + 2)
        System.arraycopy(data, 0, result, 0, data.size)
        result[data.size] = sw1
        result[data.size + 1] = sw2
        return result
    }

    companion object {
        val SUCCESS_STATUS_BYTES: ByteArray = byteArrayOf(0x90.toByte(), 0x00)

        fun success(data: ByteArray = ByteArray(0)): ResponseApdu =
            ResponseApdu(data = data, sw1 = 0x90.toByte(), sw2 = 0x00)

        fun error(sw: Int): ResponseApdu =
            ResponseApdu(
                data = ByteArray(0),
                sw1 = ((sw ushr 8) and 0xFF).toByte(),
                sw2 = (sw and 0xFF).toByte()
            )

        fun fromBytes(bytes: ByteArray): ResponseApdu {
            require(bytes.size >= 2) {
                "Response APDU must be at least 2 bytes (SW1-SW2), got ${bytes.size}"
            }
            val data = bytes.copyOfRange(0, bytes.size - 2)
            val sw1 = bytes[bytes.size - 2]
            val sw2 = bytes[bytes.size - 1]
            return ResponseApdu(data = data, sw1 = sw1, sw2 = sw2)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResponseApdu
        if (!data.contentEquals(other.data)) return false
        if (sw1 != other.sw1) return false
        if (sw2 != other.sw2) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + sw1.toInt()
        result = 31 * result + sw2.toInt()
        return result
    }
}

/**
 * Reusable builders for S-MAPLE card APDU commands.
 */
object CardCommands {

    /**
     * S-MAPLE Application Identifier (instance AID): F0 12 34 56 78 21.
     * Used by both physical smartcards and Host Card Emulation (HCE).
     */
    val SMAPLE_AID: ByteArray = byteArrayOf(
        0xF0.toByte(), 0x12.toByte(), 0x34.toByte(), 0x56.toByte(), 0x78.toByte(), 0x21.toByte()
    )

    /**
     * Builds SELECT S-MAPLE AID APDU (Case 3: 00 A4 04 00 06 F0 12 34 56 78 21).
     * @param aid The application identifier to select. Defaults to [SMAPLE_AID].
     */
    fun selectApplication(aid: ByteArray = SMAPLE_AID): CommandApdu = CommandApdu(
        cla = 0x00.toByte(),
        ins = 0xA4.toByte(),
        p1 = 0x04.toByte(),
        p2 = 0x00.toByte(),
        data = aid
    )

    /**
     * Builds GET_BASIC_INFO APDU (Case 2: 80 02 00 00 00).
     * Retrieves basic info block from mCard (at most 58 bytes).
     */
    fun getBasicInfo(): CommandApdu = CommandApdu(
        cla = ApduConstants.CLA,
        ins = ApduConstants.INS_GET_BASIC_INFO,
        p1 = 0x00,
        p2 = 0x00,
        le = 0x00
    )

    /**
     * Builds GET_INFO APDU (Case 2: 80 00 00 00 00).
     * Returns 11 bytes of card capacity and status metadata.
     */
    fun getInfo(): CommandApdu = CommandApdu(
        cla = ApduConstants.CLA,
        ins = ApduConstants.INS_GET_INFO,
        p1 = 0x00,
        p2 = 0x00,
        le = 0x00
    )

    /**
     * Builds GET_RECORDS APDU (Case 2: 80 12 P1 P2 00).
     * @param startIndex Start index as uint16 (0 is the newest record).
     */
    fun getRecords(startIndex: Int): CommandApdu {
        require(startIndex in 0..65535) { "Start index must be uint16 (0..65535), got $startIndex" }
        val p1 = ((startIndex ushr 8) and 0xFF).toByte()
        val p2 = (startIndex and 0xFF).toByte()
        return CommandApdu(
            cla = ApduConstants.CLA,
            ins = ApduConstants.INS_GET_RECORDS,
            p1 = p1,
            p2 = p2,
            le = 0x00
        )
    }

    /**
     * Builds ADD_RECORD APDU (Case 4: 80 10 00 00 Lc blob 00).
     */
    fun addRecord(recordBlob: ByteArray): CommandApdu = CommandApdu(
        cla = ApduConstants.CLA,
        ins = ApduConstants.INS_ADD_RECORD,
        p1 = 0x00,
        p2 = 0x00,
        data = recordBlob,
        le = 0x00
    )

    /**
     * Builds DEV_WRITE_BASIC_INFO APDU (Case 3: 80 E0 00 00 Lc block).
     */
    fun devWriteBasicInfo(basicInfoBlock: ByteArray): CommandApdu = CommandApdu(
        cla = ApduConstants.CLA,
        ins = ApduConstants.INS_DEV_WRITE_BASIC_INFO,
        p1 = 0x00,
        p2 = 0x00,
        data = basicInfoBlock
    )
}
