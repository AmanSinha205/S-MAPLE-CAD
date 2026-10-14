package org.smaple.cad.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.smaple.cad.reader.AndroidNfcCardReaderManager
import org.smaple.cad.reader.CardReaderManager
import javax.inject.Singleton

/**
 * Hilt module configuring [CardReaderManager] binding for CAD app.
 * Production binds [AndroidNfcCardReaderManager].
 * [org.smaple.cad.reader.FakeCardReaderManager] remains available via direct injection for tests.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ReaderModule {

    @Binds
    @Singleton
    abstract fun bindCardReaderManager(
        nfcCardReaderManager: AndroidNfcCardReaderManager
    ): CardReaderManager
}