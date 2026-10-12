package org.smaple.core.model

/**
 * Immutable blood pressure reading (systolic and diastolic in mmHg).
 *
 * Supported ranges per S-MAPLE specification:
 * - Systolic: 40..295 mmHg
 * - Diastolic: 20..275 mmHg
 */
data class BloodPressure(
    val systolic: Int,
    val diastolic: Int
) {
    init {
        require(systolic in MIN_SYSTOLIC..MAX_SYSTOLIC) {
            "Systolic BP must be between $MIN_SYSTOLIC and $MAX_SYSTOLIC mmHg (got $systolic)"
        }
        require(diastolic in MIN_DIASTOLIC..MAX_DIASTOLIC) {
            "Diastolic BP must be between $MIN_DIASTOLIC and $MAX_DIASTOLIC mmHg (got $diastolic)"
        }
    }

    /**
     * Formats blood pressure reading as standard medical notation (e.g., "120/80").
     */
    fun formatted(): String = "$systolic/$diastolic"

    companion object {
        const val MIN_SYSTOLIC = 40
        const val MAX_SYSTOLIC = 295
        const val MIN_DIASTOLIC = 20
        const val MAX_DIASTOLIC = 275
    }
}
