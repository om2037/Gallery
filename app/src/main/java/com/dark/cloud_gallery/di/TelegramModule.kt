package com.dark.cloud_gallery.di

import android.content.Context
import com.dark.cloud_gallery.data.remote.TelegramClient
import com.dark.cloud_gallery.data.remote.TelegramClientImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TelegramModule {

    @Provides
    @Singleton
    fun provideTelegramClient(
        @ApplicationContext context: Context
    ): TelegramClient {
        return TelegramClientImpl(context)
    }
}
