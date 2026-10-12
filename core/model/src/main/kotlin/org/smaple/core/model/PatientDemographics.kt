package org.smaple.core.model

import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit

/**
 * Immutable patient demographic data matching S-MAPLE card Basic Info block.
 *
 * Constraints per S-MAPLE specification:
 * - [patientId]: 1 to 12 printable ASCII characters
 * - [name]: 1 to 40 printable ASCII characters
 * - [gender]: [Gender]
 * - [bloodGroup]: [BloodGroup]
 * - [dateOfBirth]: LocalDate between 1900-01-01 and 2079-06-05 (uint16 days)
 */
data class PatientDemographics(
    val patientId: String,
    val name: String,
    val gender: Gender,
    val bloodGroup: BloodGroup,
    val dateOfBirth: LocalDate
) {
    init {
        require(patientId.length in MIN_PATIENT_ID_LENGTH..MAX_PATIENT_ID_LENGTH) {
            "Patient ID length must be between $MIN_PATIENT_ID_LENGTH and $MAX_PATIENT_ID_LENGTH (got ${patientId.length})"
        }
        require(patientId.all { it.code in ASCII_PRINTABLE_MIN..ASCII_PRINTABLE_MAX }) {
            "Patient ID must contain only printable ASCII characters (20h-7Eh)"
        }
        require(name.length in MIN_NAME_LENGTH..MAX_NAME_LENGTH) {
            "Name length must be between $MIN_NAME_LENGTH and $MAX_NAME_LENGTH (got ${name.length})"
        }
        require(name.all { it.code in ASCII_PRINTABLE_MIN..ASCII_PRINTABLE_MAX }) {
            "Name must contain only printable ASCII characters (20h-7Eh)"
        }

        val daysSince1900 = ChronoUnit.DAYS.between(EPOCH_START, dateOfBirth)
        require(daysSince1900 in 0..MAX_DAYS_SINCE_1900) {
            "Date of birth must be between $EPOCH_START and $EPOCH_END (got $dateOfBirth)"
        }
    }

    /**
     * Calculates current age in years given a reference date.
     */
    fun calculateAge(asOf: LocalDate = LocalDate.now()): Int =
        Period.between(dateOfBirth, asOf).years

    /**
     * Returns the days since 1900-01-01 as uint16 representation for card encoding.
     */
    fun daysSince1900(): Int =
        ChronoUnit.DAYS.between(EPOCH_START, dateOfBirth).toInt()

    companion object {
        const val MIN_PATIENT_ID_LENGTH = 1
        const val MAX_PATIENT_ID_LENGTH = 12
        const val MIN_NAME_LENGTH = 1
        const val MAX_NAME_LENGTH = 40
        private const val ASCII_PRINTABLE_MIN = 0x20
        private const val ASCII_PRINTABLE_MAX = 0x7E

        val EPOCH_START: LocalDate = LocalDate.of(1900, 1, 1)
        val EPOCH_END: LocalDate = LocalDate.of(2079, 6, 5)
        const val MAX_DAYS_SINCE_1900 = 65535L
    }
}
