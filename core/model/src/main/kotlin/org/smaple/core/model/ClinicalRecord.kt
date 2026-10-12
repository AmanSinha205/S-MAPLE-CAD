package org.smaple.core.model

import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

/**
 * Immutable clinical record snapshot matching S-MAPLE card Record Blob layout.
 *
 * Constraints per S-MAPLE specification:
 * - [timestamp]: UTC Instant, between 2025-01-01 00:00:00Z and 2056-12-31 23:59:00Z (uint24 minutes)
 * - [hpId]: Healthcare Professional ID, 1 to 8 printable ASCII characters
 * - [vitalSigns]: [VitalSigns]
 * - [medications]: List of medication names, combined ASCII length 1 to 60 chars separated by ';',
 *   no empty items, no items containing ';'
 * - [description]: Optional clinical note, 1 to 100 printable ASCII characters
 * - At least one data field (vital signs component, medications, or description) must be present.
 */
data class ClinicalRecord(
    val timestamp: Instant,
    val hpId: String,
    val vitalSigns: VitalSigns = VitalSigns(),
    val medications: List<String> = emptyList(),
    val description: String? = null
) {
    init {
        require(hpId.length in MIN_HP_ID_LENGTH..MAX_HP_ID_LENGTH) {
            "HP ID length must be between $MIN_HP_ID_LENGTH and $MAX_HP_ID_LENGTH (got ${hpId.length})"
        }
        require(hpId.all { it.code in ASCII_PRINTABLE_MIN..ASCII_PRINTABLE_MAX }) {
            "HP ID must contain only printable ASCII characters (20h-7Eh)"
        }

        val minutesSinceEpoch = ChronoUnit.MINUTES.between(EPOCH_START, timestamp)
        require(minutesSinceEpoch in 0..MAX_EPOCH_MINUTES) {
            "Record timestamp must be between $EPOCH_START and 2056-12 (got $timestamp)"
        }

        if (medications.isNotEmpty()) {
            medications.forEach { med ->
                require(med.isNotBlank()) { "Medication item must not be blank" }
                require(!med.contains(';')) { "Medication item must not contain semicolon ';'" }
                require(med.all { it.code in ASCII_PRINTABLE_MIN..ASCII_PRINTABLE_MAX }) {
                    "Medication item must contain only printable ASCII characters: $med"
                }
            }
            val joined = medications.joinToString(";")
            require(joined.length in MIN_MEDICATIONS_LENGTH..MAX_MEDICATIONS_LENGTH) {
                "Serialized medications length must be between $MIN_MEDICATIONS_LENGTH and $MAX_MEDICATIONS_LENGTH (got ${joined.length})"
            }
        }

        description?.let { desc ->
            require(desc.length in MIN_DESCRIPTION_LENGTH..MAX_DESCRIPTION_LENGTH) {
                "Description length must be between $MIN_DESCRIPTION_LENGTH and $MAX_DESCRIPTION_LENGTH (got ${desc.length})"
            }
            require(desc.all { it.code in ASCII_PRINTABLE_MIN..ASCII_PRINTABLE_MAX }) {
                "Description must contain only printable ASCII characters"
            }
        }

        val hasVitals = vitalSigns.hasAnyMeasurement
        val hasMeds = medications.isNotEmpty()
        val hasDesc = !description.isNullOrEmpty()

        require(hasVitals || hasMeds || hasDesc) {
            "At least one presence flag (vital signs, medications, or description) must be present in clinical record"
        }
    }

    /**
     * Calculates the uint24 minutes since 2025-01-01 00:00:00 UTC.
     */
    fun minutesSince2025Epoch(): Long =
        ChronoUnit.MINUTES.between(EPOCH_START, timestamp)

    companion object {
        const val MIN_HP_ID_LENGTH = 1
        const val MAX_HP_ID_LENGTH = 8
        const val MIN_MEDICATIONS_LENGTH = 1
        const val MAX_MEDICATIONS_LENGTH = 60
        const val MIN_DESCRIPTION_LENGTH = 1
        const val MAX_DESCRIPTION_LENGTH = 100

        private const val ASCII_PRINTABLE_MIN = 0x20
        private const val ASCII_PRINTABLE_MAX = 0x7E

        val EPOCH_START: Instant = Instant.parse("2025-01-01T00:00:00Z")
        const val MAX_EPOCH_MINUTES = 0xFFFFFFL // 24-bit unsigned maximum: 16,777,215 minutes (~31.9 years)
    }
}
