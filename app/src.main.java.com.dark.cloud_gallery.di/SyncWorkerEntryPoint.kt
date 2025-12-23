package com.dark.cloud_gallery.di

import com.dark.cloud_gallery.data.local.MediaItemDao
import com.dark.cloud_gallery.data.local.SessionManager
import com.dark.cloud_gallery.data.remote.TelegramClient
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SyncWorkerEntryPoint {
    fun telegramClient(): TelegramClient
    fun sessionManager(): SessionManager
    fun mediaItemDao(): MediaItemDao
}
