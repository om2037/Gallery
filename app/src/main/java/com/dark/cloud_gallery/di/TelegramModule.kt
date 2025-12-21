package com.dark.cloud_gallery.di

import com.dark.cloud_gallery.data.remote.TelegramClient
import com.dark.cloud_gallery.data.remote.TelegramClientImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TelegramModule {

    @Binds
    @Singleton
    abstract fun bindTelegramClient(
        telegramClientImpl: TelegramClientImpl
    ): TelegramClient
}
