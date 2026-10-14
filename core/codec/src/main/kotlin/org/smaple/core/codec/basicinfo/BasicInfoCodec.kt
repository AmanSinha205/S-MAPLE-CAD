package org.smaple.core.codec.basicinfo

import org.smaple.core.codec.CodecResult
import org.smaple.core.codec.ProtocolCodec
import org.smaple.core.codec.util.ByteBuffers
import org.smaple.core.model.BasicInfo
import org.smaple.core.model.BloodGroup
import org.smaple.core.model.Gender
import java.io.ByteArrayOutputStream

/**
 * Interface contract for encoding and decoding the S-MAPLE Basic Info block.
 * Per specification: Total at most 58 bytes.
 */
interface BasicInfoCodec : ProtocolCodec<BasicInfo>

/**
 * Production-safe default implementation of [BasicInfoCodec].
 * Encodes and decodes the Basic Info block per S-MAPLE card command specification (v1).
 *
 * Supports decoding both:
 * 1. Raw Basic Info block payload (at most 58 bytes).
 * 2. Full GET_BASIC_INFO response APDU (payload followed by success status word 90 00).
 */
class DefaultBasicInfoCodec : BasicInfoCodec {

    override fun encode(value: BasicInfo): ByteArray {
        val stream = ByteArrayOutputStream()

        // Byte 0: Format 0x10 (version 1 in high nibble, 0 in low nibble)
        stream.write(FORMAT_VERSION_1)

        // Byte 1: bits 7-6 gender, bits 5-2 blood group, bits 1-0 zero
        val genderBits = (value.gender.code and 0x03) shl 6
        val bloodBits = (value.bloodGroup.code and 0x0F) shl 2
        stream.write(genderBits or bloodBits)

        // Bytes 2-3: Date of birth (uint16, days since 1900-01-01)
        val days = value.daysSince1900()
        val dobBytes = ByteArray(2)
        ByteBuffers.putUint16BigEndian(dobBytes, 0, days)
        stream.write(dobBytes)

        // Bytes 4..: Patient ID (length 1-12 then ASCII)
        val patIdBytes = ByteBuffers.encodeAscii(value.patientId)
        stream.write(patIdBytes.size)
        stream.write(patIdBytes)

        // Next..: Name (length 1-40 then ASCII)
        val nameBytes = ByteBuffers.encodeAscii(value.name)
        stream.write(nameBytes.size)
        stream.write(nameBytes)

        return stream.toByteArray()
    }

    override fun decode(bytes: ByteArray): CodecResult<BasicInfo> {
        if (bytes.isEmpty()) {
            return CodecResult.Error("Basic info block is empty")
        }

        // If response is exactly 2 bytes, it is likely a standalone APDU status word
        if (bytes.size == 2) {
            val sw = ByteBuffers.getUint16BigEndian(bytes, 0)
            return when (sw) {
                0x6A88 -> CodecResult.Error("Card returned status word 6A88: Data not found (Basic info has not been written)")
                0x6982 -> CodecResult.Error("Card returned status word 6982: Security status not satisfied (no secure session)")
                0x6985 -> CodecResult.Error("Card returned status word 6985: Conditions not satisfied")
                0x6700 -> CodecResult.Error("Card returned status word 6700: Wrong length")
                0x6A80 -> CodecResult.Error("Card returned status word 6A80: Incorrect data")
                0x6A86 -> CodecResult.Error("Card returned status word 6A86: Incorrect P1/P2")
                0x6D00 -> CodecResult.Error("Card returned status word 6D00: Instruction code not supported")
                0x6E00 -> CodecResult.Error("Card returned status word 6E00: Class not supported")
                else -> CodecResult.Error("Card returned error status word: 0x${sw.toString(16).uppercase().padStart(4, '0')}")
            }
        }

        // Check if bytes represent an APDU response with trailing SW1-SW2 0x90 0x00
        val payload = if (bytes.size >= MIN_BASIC_INFO_BYTES + 2 &&
            bytes[bytes.size - 2] == 0x90.toByte() &&
            bytes[bytes.size - 1] == 0x00.toByte()
        ) {
            bytes.copyOfRange(0, bytes.size - 2)
        } else {
            bytes
        }

        if (payload.size < MIN_BASIC_INFO_BYTES) {
            return CodecResult.Error("Basic info block too short: expected at least $MIN_BASIC_INFO_BYTES bytes, got ${payload.size}")
        }
        if (payload.size > MAX_BASIC_INFO_BYTES) {
            return CodecResult.Error("Basic info block exceeds maximum length $MAX_BASIC_INFO_BYTES: got ${payload.size}")
        }

        val formatByte = payload[0].toInt() and 0xFF
        if (formatByte != FORMAT_VERSION_1) {
            return CodecResult.Error("Unsupported format byte: 0x${formatByte.toString(16).padStart(2, '0')}")
        }

        val flagsByte = payload[1].toInt() and 0xFF
        val reservedBits = flagsByte and 0x03
        if (reservedBits != 0) {
            return CodecResult.Error("Reserved bits in flags byte must be zero, got $reservedBits")
        }

        val genderCode = (flagsByte ushr 6) and 0x03
        val bloodCode = (flagsByte ushr 2) and 0x0F

        val gender = try {
            Gender.fromCode(genderCode)
        } catch (e: IllegalArgumentException) {
            return CodecResult.Error("Invalid gender code: $genderCode", e)
        }

        val bloodGroup = try {
            BloodGroup.fromCode(bloodCode)
        } catch (e: IllegalArgumentException) {
            return CodecResult.Error("Invalid blood group code: $bloodCode", e)
        }

        val daysSince1900 = ByteBuffers.getUint16BigEndian(payload, 2)
        val dob = BasicInfo.EPOCH_START.plusDays(daysSince1900.toLong())

        var offset = 4
        val patIdLen = payload[offset].toInt() and 0xFF
        offset += 1

        if (patIdLen !in BasicInfo.MIN_PATIENT_ID_LENGTH..BasicInfo.MAX_PATIENT_ID_LENGTH) {
            return CodecResult.Error("Invalid patient ID length: $patIdLen")
        }
        if (offset + patIdLen > payload.size) {
            return CodecResult.Error("Truncated patient ID: expected $patIdLen bytes, remaining ${payload.size - offset}")
        }

        for (i in offset until (offset + patIdLen)) {
            val b = payload[i].toInt() and 0xFF
            if (b !in ASCII_PRINTABLE_MIN..ASCII_PRINTABLE_MAX) {
                return CodecResult.Error("Patient ID contains non-printable ASCII character: 0x${b.toString(16).padStart(2, '0')}")
            }
        }

        val patientId = ByteBuffers.decodeAscii(payload, offset, patIdLen)
        offset += patIdLen

        if (offset >= payload.size) {
            return CodecResult.Error("Missing name length byte in basic info block")
        }

        val nameLen = payload[offset].toInt() and 0xFF
        offset += 1

        if (nameLen !in BasicInfo.MIN_NAME_LENGTH..BasicInfo.MAX_NAME_LENGTH) {
            return CodecResult.Error("Invalid name length: $nameLen")
        }
        if (offset + nameLen > payload.size) {
            return CodecResult.Error("Truncated name: expected $nameLen bytes, remaining ${payload.size - offset}")
        }

        for (i in offset until (offset + nameLen)) {
            val b = payload[i].toInt() and 0xFF
            if (b !in ASCII_PRINTABLE_MIN..ASCII_PRINTABLE_MAX) {
                return CodecResult.Error("Name contains non-printable ASCII character: 0x${b.toString(16).padStart(2, '0')}")
            }
        }

        val name = ByteBuffers.decodeAscii(payload, offset, nameLen)
        offset += nameLen

        if (offset != payload.size) {
            return CodecResult.Error("Unexpected trailing bytes in basic info block: ${payload.size - offset} extra bytes")
        }

        return try {
            CodecResult.Success(
                BasicInfo(
                    patientId = patientId,
                    name = name,
                    gender = gender,
                    bloodGroup = bloodGroup,
                    dateOfBirth = dob
                )
            )
        } catch (e: Exception) {
            CodecResult.Error("Failed to construct BasicInfo: ${e.message}", e)
        }
    }

    companion object {
        const val FORMAT_VERSION_1 = 0x10
        const val MIN_BASIC_INFO_BYTES = 6
        const val MAX_BASIC_INFO_BYTES = 58
        private const val ASCII_PRINTABLE_MIN = 0x20
        private const val ASCII_PRINTABLE_MAX = 0x7E
    }
}
