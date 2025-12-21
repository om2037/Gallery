package com.dark.cloud_gallery.domain.repository

import com.dark.cloud_gallery.domain.model.MediaItem
import kotlinx.coroutines.flow.Flow

interface MediaRepository {

    fun getAllMediaItems(): Flow<List<MediaItem>>

    fun getMediaItemsByDevice(deviceModel: String): Flow<List<MediaItem>>

    fun getDeviceModels(): Flow<List<String>>

    suspend fun getMediaItemById(id: Long): MediaItem?

    suspend fun insertMediaItem(mediaItem: MediaItem)

    suspend fun updateMediaItem(mediaItem: MediaItem)

    suspend fun downloadMediaItem(mediaItem: MediaItem)
}
