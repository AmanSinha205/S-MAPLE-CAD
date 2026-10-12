package org.smaple.core.model

/**
 * Immutable patient vital signs snapshot.
 *
 * Supported ranges per S-MAPLE specification:
 * - Blood Pressure: [BloodPressure]
 * - Body Temperature: 30.0°C .. 55.5°C
 * - Heart Rate: 0 .. 255 bpm
 */
data class VitalSigns(
    val bloodPressure: BloodPressure? = null,
    val temperatureCelsius: Float? = null,
    val heartRateBpm: Int? = null
) {
    init {
        temperatureCelsius?.let { temp ->
            require(temp in MIN_TEMPERATURE_CELSIUS..MAX_TEMPERATURE_CELSIUS) {
                "Temperature must be between $MIN_TEMPERATURE_CELSIUS and $MAX_TEMPERATURE_CELSIUS °C (got $temp)"
            }
        }
        heartRateBpm?.let { hr ->
            require(hr in MIN_HEART_RATE..MAX_HEART_RATE) {
                "Heart rate must be between $MIN_HEART_RATE and $MAX_HEART_RATE bpm (got $hr)"
            }
        }
    }

    /**
     * True if at least one vital measurement is present.
     */
    val hasAnyMeasurement: Boolean
        get() = bloodPressure != null || temperatureCelsius != null || heartRateBpm != null

    companion object {
        const val MIN_TEMPERATURE_CELSIUS = 30.0f
        const val MAX_TEMPERATURE_CELSIUS = 55.5f
        const val MIN_HEART_RATE = 0
        const val MAX_HEART_RATE = 255
    }
}
