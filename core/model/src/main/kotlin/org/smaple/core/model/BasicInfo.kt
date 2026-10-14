package org.smaple.core.model

/**
 * BasicInfo domain model representing the patient's basic information stored on the card.
 * Aligns with S-MAPLE card command specification (v1) section 3 (Basic info block).
 *
 * Represents:
 * - [patientId]: Unique patient identifier (1 to 12 printable ASCII characters)
 * - [name]: Full patient name (1 to 40 printable ASCII characters)
 * - [gender]: Patient gender ([Gender])
 * - [dateOfBirth]: Date of birth (LocalDate, between 1900-01-01 and 2079-06-05)
 * - [bloodGroup]: ABO/Rh blood group ([BloodGroup])
 */
typealias BasicInfo = PatientDemographics
