package org.smaple.core.model

/**
 * Patient gender representation matching S-MAPLE card command specification (v1).
 *
 * Encoded as a 2-bit value:
 * - 0: Male
 * - 1: Female
 * - 2: Non-binary
 * - 3: Not stated
 */
enum class Gender(val code: Int) {
    MALE(0),
    FEMALE(1),
    NON_BINARY(2),
    NOT_STATED(3);

    companion object {
        fun fromCode(code: Int): Gender =
            entries.firstOrNull { it.code == code }
                ?: throw IllegalArgumentException("Unknown gender code: $code")
    }
}
