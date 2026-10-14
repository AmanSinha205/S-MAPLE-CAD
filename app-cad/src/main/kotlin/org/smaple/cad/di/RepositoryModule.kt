package org.smaple.cad.di

import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.smaple.cad.data.repository.FakePatientRepository
import org.smaple.cad.data.repository.NfcPatientRepository
import org.smaple.cad.data.repository.PatientRepository
import org.smaple.core.codec.basicinfo.BasicInfoCodec
import org.smaple.core.codec.basicinfo.DefaultBasicInfoCodec
import javax.inject.Singleton

/**
 * Hilt module configuring repository bindings for the CAD app.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    /**
     * Switch point between fake repository and real NFC repository.
     *
     * Set to `false` for Phase 2 production to use [NfcPatientRepository] with real NFC communication.
     * Set to `true` to revert to [FakePatientRepository] simulated delay and mock data.
     */
    const val USE_FAKE_PATIENT_REPOSITORY: Boolean = false

    @Provides
    @Singleton
    fun providePatientRepository(
        fake: FakePatientRepository,
        real: Lazy<NfcPatientRepository>
    ): PatientRepository {
        return if (USE_FAKE_PATIENT_REPOSITORY) {
            fake
        } else {
            real.get()
        }
    }

    @Provides
    @Singleton
    fun provideBasicInfoCodec(): BasicInfoCodec = DefaultBasicInfoCodec()
}
