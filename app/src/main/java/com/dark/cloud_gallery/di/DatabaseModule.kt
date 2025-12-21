package com.dark.cloud_gallery.di

import android.content.Context
import androidx.room.Room
import com.dark.cloud_gallery.data.local.AppDatabase
import com.dark.cloud_gallery.data.local.MediaItemDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "cloud_gallery_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideMediaItemDao(appDatabase: AppDatabase): MediaItemDao {
        return appDatabase.mediaItemDao()
    }
}
