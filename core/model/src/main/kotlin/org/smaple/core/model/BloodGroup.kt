package org.smaple.core.model

/**
 * Patient ABO/Rh blood group representation matching S-MAPLE card command specification (v1).
 *
 * Encoded as a 4-bit value:
 * - 0: Unknown
 * - 1: A+
 * - 2: A-
 * - 3: B+
 * - 4: B-
 * - 5: AB+
 * - 6: AB-
 * - 7: O+
 * - 8: O-
 */
enum class BloodGroup(val code: Int, val displayLabel: String) {
    UNKNOWN(0, "Unknown"),
    A_POSITIVE(1, "A+"),
    A_NEGATIVE(2, "A-"),
    B_POSITIVE(3, "B+"),
    B_NEGATIVE(4, "B-"),
    AB_POSITIVE(5, "AB+"),
    AB_NEGATIVE(6, "AB-"),
    O_POSITIVE(7, "O+"),
    O_NEGATIVE(8, "O-");

    companion object {
        fun fromCode(code: Int): BloodGroup =
            entries.firstOrNull { it.code == code }
                ?: throw IllegalArgumentException("Unknown blood group code: $code")
    }
}
