package org.smaple.cad.data.model

import java.time.LocalDateTime

/**
 * Represents a patient with detailed information used in the CAD workflow.
 */
data class Patient(
    val patientId: String,
    val name: String,
    val age: Int,
    val gender: org.smaple.core.model.Gender,
    val lastVisit: LocalDateTime,
    val medicalSummary: String,
    val riskStatus: RiskStatus
) {
    /** Simple risk classification */
    enum class RiskStatus { LOW, MEDIUM, HIGH }
}
