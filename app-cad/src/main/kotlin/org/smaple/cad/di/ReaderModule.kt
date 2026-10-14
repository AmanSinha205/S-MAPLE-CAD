package org.smaple.cad.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.smaple.cad.reader.CardReaderManager
import org.smaple.cad.reader.FakeCardReaderManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReaderModule {

    @Binds
    @Singleton
    abstract fun bindCardReaderManager(
        fakeCardReaderManager: FakeCardReaderManager
    ): CardReaderManager
}