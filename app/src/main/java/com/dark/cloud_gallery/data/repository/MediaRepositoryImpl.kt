package com.dark.cloud_gallery.data.repository

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.dark.cloud_gallery.data.local.MediaItemDao
import com.dark.cloud_gallery.data.local.SessionManager
import com.dark.cloud_gallery.data.remote.TelegramClient
import com.dark.cloud_gallery.domain.model.MediaItem
import com.dark.cloud_gallery.domain.model.SmsBackup
import com.dark.cloud_gallery.domain.repository.MediaRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.dark.cloud_gallery.data.remote.SyncWorker
import com.dark.cloud_gallery.util.Constants.SYNC_WORK_TAG
import org.drinkless.tdlib.TdApi
import java.io.File
import javax.inject.Inject

class MediaRepositoryImpl @Inject constructor(
    private val dao: MediaItemDao,
    @ApplicationContext private val context: Context,
    private val telegramClient: TelegramClient,
    private val sessionManager: SessionManager
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

    override fun getSmsBackups(): Flow<List<SmsBackup>> = flow {
        val channelId = sessionManager.getChannelId()?.toLongOrNull() ?: return@flow
        val lastSyncTimestamp = sessionManager.getLastSmsSyncTimestamp()
        val syncStartDate = sessionManager.getSyncStartDate()
        val startTimestamp = if (lastSyncTimestamp > 0) lastSyncTimestamp else syncStartDate

        var fromMessageId: Long = 0
        val allBackups = mutableListOf<SmsBackup>()
        var latestTimestamp = lastSyncTimestamp

        do {
            val messages = telegramClient.getChatHistory(channelId, fromMessageId)
            val filteredMessages = messages.messages.filter { it.date.toLong() * 1000 > startTimestamp }

            val backups = filteredMessages.mapNotNull { message ->
                if (message.content is TdApi.MessageDocument) {
                    val document = (message.content as TdApi.MessageDocument).document
                    if (document.fileName.startsWith("گزارش پیامک‌ها") && document.fileName.endsWith(".html")) {
                        val caption = (message.content as TdApi.MessageDocument).caption.text
                        val file = telegramClient.downloadFile(document.document.id)

                        if (message.date.toLong() * 1000 > latestTimestamp) {
                            latestTimestamp = message.date.toLong() * 1000
                        }

                        SmsBackup(
                            id = message.id,
                            filePath = file.local.path,
                            deviceModel = caption,
                            timestamp = message.date.toLong() * 1000
                        )
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
            allBackups.addAll(backups)
            fromMessageId = messages.messages.lastOrNull()?.id ?: 0
        } while (messages.messages.isNotEmpty() && (filteredMessages.size == messages.messages.size))

        if (latestTimestamp > lastSyncTimestamp) {
            sessionManager.saveLastSmsSyncTimestamp(latestTimestamp)
        }
        emit(allBackups)
    }

    override suspend fun syncMediaItems() {
        val workManager = WorkManager.getInstance(context)
        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .addTag(SYNC_WORK_TAG)
            .build()
        workManager.enqueue(syncWorkRequest)
    }
}
