package org.smaple.mcard.data

import org.smaple.core.model.BasicInfo
import org.smaple.core.model.BloodGroup
import org.smaple.core.model.Gender
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interface contract for providing patient [BasicInfo] to the mCard HCE service.
 */
interface BasicInfoProvider {
    /**
     * Retrieves the [BasicInfo] stored on the patient mCard, or null if not yet initialized.
     */
    fun getBasicInfo(): BasicInfo?
}

/**
 * Default sample provider supplying patient demographic data for mCard HCE card emulation.
 * Uses the reference patient Asha Verma from API.md section 8 test vectors.
 *
 * Later this will connect to persistent Room / EncryptedSharedPreferences storage.
 */
@Singleton
class SampleBasicInfoProvider @Inject constructor() : BasicInfoProvider {

    override fun getBasicInfo(): BasicInfo {
        // Reference vector from API.md section 8:
        // Asha Verma, female, B+, born 1972-05-20 (26,437 days), patient ID AV-2041 (23 bytes)
        return BasicInfo(
            patientId = "AV-2041",
            name = "Asha Verma",
            gender = Gender.FEMALE,
            bloodGroup = BloodGroup.B_POSITIVE,
            dateOfBirth = LocalDate.of(1972, 5, 20)
        )
    }
}
