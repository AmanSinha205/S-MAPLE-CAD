package org.smaple.core.cardtransport

/**
 * Central debug configuration flag for S-MAPLE NFC layer.
 *
 * ## ENABLED = true
 * - Logs full APDU bytes (hex) for every command and response.
 * - Logs all state transitions in reader and HCE service.
 * - Useful during physical device testing and integration validation.
 *
 * ## ENABLED = false (production default)
 * - Suppresses verbose APDU hex dumps.
 * - No patient data bytes logged to logcat.
 * - Safe for production builds where device logs may be accessible.
 *
 * ## Usage
 * ```kotlin
 * if (NfcDebugConfig.ENABLED) {
 *     Log.d(TAG, "APDU TX: ${ByteBuffers.toHexString(bytes)}")
 * }
 * ```
 *
 * > [!CAUTION]
 * > Do NOT ship production builds with ENABLED = true.
 * > APDU payloads contain patient PII (name, gender, blood group, DOB).
 */
object NfcDebugConfig {

    /**
     * Master switch for NFC APDU debug logging.
     *
     * Set to `true` when performing physical device NFC validation.
     * Must be `false` for any production or release build.
     */
    const val ENABLED: Boolean = true

    /**
     * If true, log each state transition (Idle -> Scanning -> CardDetected -> etc.).
     * Only active when [ENABLED] is true.
     */
    const val LOG_STATE_TRANSITIONS: Boolean = true

    /**
     * If true, include full hex dump of APDU command bytes in logs.
     * Only active when [ENABLED] is true.
     */
    const val LOG_APDU_BYTES: Boolean = true

    /**
     * If true, include full hex dump of APDU response bytes in logs.
     * This is the most sensitive: response payloads contain encoded patient PII.
     * Only active when [ENABLED] is true.
     */
    const val LOG_RESPONSE_BYTES: Boolean = true
}
