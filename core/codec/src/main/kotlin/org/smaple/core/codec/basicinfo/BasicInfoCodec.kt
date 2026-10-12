package org.smaple.core.codec.basicinfo

import org.smaple.core.codec.CodecResult
import org.smaple.core.codec.ProtocolCodec
import org.smaple.core.codec.util.ByteBuffers
import org.smaple.core.model.BloodGroup
import org.smaple.core.model.Gender
import org.smaple.core.model.PatientDemographics
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Interface contract for encoding and decoding the S-MAPLE Basic Info block.
 * Per specification: Total at most 58 bytes.
 */
interface BasicInfoCodec : ProtocolCodec<PatientDemographics>

/**
 * Production-safe default implementation of [BasicInfoCodec].
 * Encodes and decodes the Basic Info block per S-MAPLE card command specification (v1).
 */
class DefaultBasicInfoCodec : BasicInfoCodec {

    override fun encode(value: PatientDemographics): ByteArray {
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

    override fun decode(bytes: ByteArray): CodecResult<PatientDemographics> {
        if (bytes.size < MIN_BASIC_INFO_BYTES) {
            return CodecResult.Error("Basic info block too short: expected at least $MIN_BASIC_INFO_BYTES bytes, got ${bytes.size}")
        }
        if (bytes.size > MAX_BASIC_INFO_BYTES) {
            return CodecResult.Error("Basic info block exceeds maximum length $MAX_BASIC_INFO_BYTES: got ${bytes.size}")
        }

        val formatByte = bytes[0].toInt() and 0xFF
        if (formatByte != FORMAT_VERSION_1) {
            return CodecResult.Error("Unsupported format byte: 0x${formatByte.toString(16).padStart(2, '0')}")
        }

        val flagsByte = bytes[1].toInt() and 0xFF
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

        val daysSince1900 = ByteBuffers.getUint16BigEndian(bytes, 2)
        val dob = PatientDemographics.EPOCH_START.plusDays(daysSince1900.toLong())

        var offset = 4
        val patIdLen = bytes[offset].toInt() and 0xFF
        offset += 1

        if (patIdLen !in PatientDemographics.MIN_PATIENT_ID_LENGTH..PatientDemographics.MAX_PATIENT_ID_LENGTH) {
            return CodecResult.Error("Invalid patient ID length: $patIdLen")
        }
        if (offset + patIdLen > bytes.size) {
            return CodecResult.Error("Truncated patient ID: expected $patIdLen bytes, remaining ${bytes.size - offset}")
        }

        val patientId = ByteBuffers.decodeAscii(bytes, offset, patIdLen)
        offset += patIdLen

        if (offset >= bytes.size) {
            return CodecResult.Error("Missing name length byte in basic info block")
        }

        val nameLen = bytes[offset].toInt() and 0xFF
        offset += 1

        if (nameLen !in PatientDemographics.MIN_NAME_LENGTH..PatientDemographics.MAX_NAME_LENGTH) {
            return CodecResult.Error("Invalid name length: $nameLen")
        }
        if (offset + nameLen > bytes.size) {
            return CodecResult.Error("Truncated name: expected $nameLen bytes, remaining ${bytes.size - offset}")
        }

        val name = ByteBuffers.decodeAscii(bytes, offset, nameLen)

        return try {
            CodecResult.Success(
                PatientDemographics(
                    patientId = patientId,
                    name = name,
                    gender = gender,
                    bloodGroup = bloodGroup,
                    dateOfBirth = dob
                )
            )
        } catch (e: Exception) {
            CodecResult.Error("Failed to construct PatientDemographics: ${e.message}", e)
        }
    }

    companion object {
        const val FORMAT_VERSION_1 = 0x10
        const val MIN_BASIC_INFO_BYTES = 6
        const val MAX_BASIC_INFO_BYTES = 58
    }
}
