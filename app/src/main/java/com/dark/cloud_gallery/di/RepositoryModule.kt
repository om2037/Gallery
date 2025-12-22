package com.dark.cloud_gallery.di

import android.content.Context
import com.dark.cloud_gallery.data.local.MediaItemDao
import com.dark.cloud_gallery.data.local.SessionManager
import com.dark.cloud_gallery.data.remote.TelegramClient
import com.dark.cloud_gallery.data.repository.MediaRepositoryImpl
import com.dark.cloud_gallery.domain.repository.MediaRepository
import com.dark.cloud_gallery.util.FileLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideMediaRepository(
        dao: MediaItemDao,
        @ApplicationContext context: Context,
        telegramClient: TelegramClient,
        sessionManager: SessionManager,
        fileLogger: FileLogger
    ): MediaRepository {
        return MediaRepositoryImpl(
            dao = dao,
            context = context,
            telegramClient = telegramClient,
            sessionManager = sessionManager,
            fileLogger = fileLogger
        )
    }
}
