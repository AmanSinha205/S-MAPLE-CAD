package org.smaple.cad.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.smaple.cad.data.repository.FakePatientRepository
import org.smaple.cad.data.repository.PatientRepository
import javax.inject.Singleton

/** Hilt module that provides the fake patient repository. */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun providePatientRepository(fake: FakePatientRepository): PatientRepository = fake
}
