package org.smaple.core.codec.record

import org.smaple.core.codec.CodecResult
import org.smaple.core.codec.ProtocolCodec
import org.smaple.core.codec.util.ByteBuffers
import org.smaple.core.model.BloodPressure
import org.smaple.core.model.ClinicalRecord
import org.smaple.core.model.VitalSigns
import java.io.ByteArrayOutputStream
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

/**
 * Interface contract for encoding and decoding the S-MAPLE Clinical Record blob.
 * Per specification: Total at most 180 bytes.
 */
interface RecordCodec : ProtocolCodec<ClinicalRecord>

/**
 * Production-safe default implementation of [RecordCodec].
 * Encodes and decodes the record blob per S-MAPLE card command specification (v1).
 */
class DefaultRecordCodec : RecordCodec {

    override fun encode(value: ClinicalRecord): ByteArray {
        val stream = ByteArrayOutputStream()

        var flags = 0
        if (value.vitalSigns.bloodPressure != null) flags = flags or FLAG_BP
        if (value.vitalSigns.temperatureCelsius != null) flags = flags or FLAG_TEMP
        if (value.vitalSigns.heartRateBpm != null) flags = flags or FLAG_HR
        if (value.medications.isNotEmpty()) flags = flags or FLAG_MEDICATIONS
        if (!value.description.isNullOrEmpty()) flags = flags or FLAG_DESCRIPTION

        // Byte 0: Header (bits 7-5 format version 001, bits 4-0 presence flags)
        val header = FORMAT_VERSION_MASK or (flags and 0x1F)
        stream.write(header)

        // Bytes 1-3: Timestamp (uint24 minutes since 2025-01-01 00:00 UTC)
        val epochMinutes = value.minutesSince2025Epoch()
        val tsBytes = ByteArray(3)
        ByteBuffers.putUint24BigEndian(tsBytes, 0, epochMinutes)
        stream.write(tsBytes)

        // HP ID: length 1-8 then ASCII
        val hpIdBytes = ByteBuffers.encodeAscii(value.hpId)
        stream.write(hpIdBytes.size)
        stream.write(hpIdBytes)

        // BP: systolic - 40, diastolic - 20
        value.vitalSigns.bloodPressure?.let { bp ->
            stream.write(bp.systolic - 40)
            stream.write(bp.diastolic - 20)
        }

        // Temperature: tenths of a degree Celsius - 300
        value.vitalSigns.temperatureCelsius?.let { temp ->
            val tenths = (temp * 10f).roundToInt()
            stream.write(tenths - 300)
        }

        // Heart rate: 0 to 255 bpm
        value.vitalSigns.heartRateBpm?.let { hr ->
            stream.write(hr)
        }

        // Medications: length then ASCII items joined with ';'
        if (value.medications.isNotEmpty()) {
            val joined = value.medications.joinToString(";")
            val medBytes = ByteBuffers.encodeAscii(joined)
            stream.write(medBytes.size)
            stream.write(medBytes)
        }

        // Description: length then ASCII
        value.description?.let { desc ->
            if (desc.isNotEmpty()) {
                val descBytes = ByteBuffers.encodeAscii(desc)
                stream.write(descBytes.size)
                stream.write(descBytes)
            }
        }

        val result = stream.toByteArray()
        require(result.size <= MAX_RECORD_BYTES) {
            "Encoded record blob exceeds maximum 180 bytes (size: ${result.size})"
        }
        return result
    }

    override fun decode(bytes: ByteArray): CodecResult<ClinicalRecord> {
        if (bytes.isEmpty()) {
            return CodecResult.Error("Empty record bytes")
        }
        if (bytes.size > MAX_RECORD_BYTES) {
            return CodecResult.Error("Record bytes exceed maximum 180 bytes (got ${bytes.size})")
        }

        val header = bytes[0].toInt() and 0xFF
        val version = (header ushr 5) and 0x07
        if (version != 1) {
            return CodecResult.Error("Invalid record format version: expected 1 (001b), got $version")
        }

        val flags = header and 0x1F
        if (flags == 0) {
            return CodecResult.Error("Invalid record: at least one presence flag must be set")
        }

        if (bytes.size < 5) {
            return CodecResult.Error("Record too short for header, timestamp, and HP ID length")
        }

        val epochMinutes = ByteBuffers.getUint24BigEndian(bytes, 1)
        val timestamp = ClinicalRecord.EPOCH_START.plus(epochMinutes, ChronoUnit.MINUTES)

        var offset = 4
        val hpIdLen = bytes[offset].toInt() and 0xFF
        offset += 1

        if (hpIdLen !in ClinicalRecord.MIN_HP_ID_LENGTH..ClinicalRecord.MAX_HP_ID_LENGTH) {
            return CodecResult.Error("Invalid HP ID length: $hpIdLen")
        }
        if (offset + hpIdLen > bytes.size) {
            return CodecResult.Error("Truncated HP ID in record blob")
        }

        val hpId = ByteBuffers.decodeAscii(bytes, offset, hpIdLen)
        offset += hpIdLen

        // BP
        val bp = if ((flags and FLAG_BP) != 0) {
            if (offset + 2 > bytes.size) return CodecResult.Error("Truncated BP in record blob")
            val sysRaw = bytes[offset].toInt() and 0xFF
            val diaRaw = bytes[offset + 1].toInt() and 0xFF
            offset += 2
            BloodPressure(systolic = sysRaw + 40, diastolic = diaRaw + 20)
        } else null

        // Temperature
        val temp = if ((flags and FLAG_TEMP) != 0) {
            if (offset >= bytes.size) return CodecResult.Error("Truncated temperature in record blob")
            val tempRaw = bytes[offset].toInt() and 0xFF
            offset += 1
            (tempRaw + 300) / 10.0f
        } else null

        // Heart rate
        val hr = if ((flags and FLAG_HR) != 0) {
            if (offset >= bytes.size) return CodecResult.Error("Truncated heart rate in record blob")
            val hrRaw = bytes[offset].toInt() and 0xFF
            offset += 1
            hrRaw
        } else null

        // Medications
        val meds = if ((flags and FLAG_MEDICATIONS) != 0) {
            if (offset >= bytes.size) return CodecResult.Error("Missing medications length in record blob")
            val medLen = bytes[offset].toInt() and 0xFF
            offset += 1
            if (offset + medLen > bytes.size) return CodecResult.Error("Truncated medications in record blob")
            val joined = ByteBuffers.decodeAscii(bytes, offset, medLen)
            offset += medLen
            joined.split(';')
        } else emptyList()

        // Description
        val desc = if ((flags and FLAG_DESCRIPTION) != 0) {
            if (offset >= bytes.size) return CodecResult.Error("Missing description length in record blob")
            val descLen = bytes[offset].toInt() and 0xFF
            offset += 1
            if (offset + descLen > bytes.size) return CodecResult.Error("Truncated description in record blob")
            val d = ByteBuffers.decodeAscii(bytes, offset, descLen)
            offset += descLen
            d
        } else null

        return try {
            val record = ClinicalRecord(
                timestamp = timestamp,
                hpId = hpId,
                vitalSigns = VitalSigns(
                    bloodPressure = bp,
                    temperatureCelsius = temp,
                    heartRateBpm = hr
                ),
                medications = meds,
                description = desc
            )
            CodecResult.Success(record)
        } catch (e: Exception) {
            CodecResult.Error("Failed to construct ClinicalRecord from decoded data: ${e.message}", e)
        }
    }

    companion object {
        const val FORMAT_VERSION_MASK = 0x20 // 0010 0000 (v1)
        const val FLAG_BP = 0x10
        const val FLAG_TEMP = 0x08
        const val FLAG_HR = 0x04
        const val FLAG_MEDICATIONS = 0x02
        const val FLAG_DESCRIPTION = 0x01
        const val MAX_RECORD_BYTES = 180
    }
}
