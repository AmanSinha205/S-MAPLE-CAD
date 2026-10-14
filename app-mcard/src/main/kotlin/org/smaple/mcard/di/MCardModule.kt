package org.smaple.mcard.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.smaple.core.codec.basicinfo.BasicInfoCodec
import org.smaple.core.codec.basicinfo.DefaultBasicInfoCodec
import org.smaple.mcard.data.BasicInfoProvider
import org.smaple.mcard.data.SampleBasicInfoProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MCardModule {

    @Binds
    @Singleton
    abstract fun bindBasicInfoProvider(
        provider: SampleBasicInfoProvider
    ): BasicInfoProvider

    companion object {
        @Provides
        @Singleton
        fun provideBasicInfoCodec(): BasicInfoCodec = DefaultBasicInfoCodec()
    }
}
