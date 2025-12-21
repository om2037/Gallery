package com.dark.cloud_gallery.data.repository

import com.dark.cloud_gallery.data.local.MediaItemDao
import com.dark.cloud_gallery.domain.model.MediaItem
import com.dark.cloud_gallery.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MediaRepositoryImpl @Inject constructor(
    private val dao: MediaItemDao
) : MediaRepository {

    override fun getAllMediaItems(): Flow<List<MediaItem>> {
        return dao.getAllMediaItems()
    }

    override fun getMediaItemsByDevice(deviceModel: String): Flow<List<MediaItem>> {
        return dao.getMediaItemsByDevice(deviceModel)
    }

    override fun getDeviceModels(): Flow<List<String>> {
        return dao.getDeviceModels()
    }

    override suspend fun getMediaItemById(id: Long): MediaItem? {
        return dao.getMediaItemById(id)
    }

    override suspend fun insertMediaItem(mediaItem: MediaItem) {
        dao.insert(mediaItem)
    }

    override suspend fun updateMediaItem(mediaItem: MediaItem) {
        dao.update(mediaItem)
    }
}
