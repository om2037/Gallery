package com.dark.cloud_gallery.data.repository

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.dark.cloud_gallery.data.local.MediaItemDao
import com.dark.cloud_gallery.domain.model.MediaItem
import com.dark.cloud_gallery.domain.repository.MediaRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject

class MediaRepositoryImpl @Inject constructor(
    private val dao: MediaItemDao,
    @ApplicationContext private val context: Context
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

    override suspend fun downloadMediaItem(mediaItem: MediaItem) {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "photo_${mediaItem.id}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/CloudGallery")
            }
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            resolver.openOutputStream(it).use { outputStream ->
                val file = File(mediaItem.filePath)
                file.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream!!)
                }
            }
        }
    }
}
