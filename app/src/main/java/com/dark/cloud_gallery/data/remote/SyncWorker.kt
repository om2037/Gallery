package com.dark.cloud_gallery.data.remote

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.dark.cloud_gallery.domain.repository.MediaRepository
import com.dark.cloud_gallery.domain.usecase.CaptionParser
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.drinkless.tdlib.TdApi

import com.dark.cloud_gallery.data.local.MediaItemDao
import com.dark.cloud_gallery.data.local.SessionManager
import com.dark.cloud_gallery.domain.model.MediaItem

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val telegramClient: TelegramClient,
    private val sessionManager: SessionManager,
    private val dao: MediaItemDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val channelId = sessionManager.getChannelId()?.toLongOrNull() ?: return@withContext Result.failure()
            val lastSyncTimestamp = sessionManager.getLastMediaSyncTimestamp()
            val syncStartDate = sessionManager.getSyncStartDate()
            val startTimestamp = if (lastSyncTimestamp > 0) lastSyncTimestamp else syncStartDate

            var fromMessageId: Long = 0
            var latestTimestamp = lastSyncTimestamp
            var totalFound = 0
            var downloadedCount = 0

            do {
                val messages = telegramClient.getChatHistory(channelId, fromMessageId)
                val filteredMessages = messages.messages.filter { it.date.toLong() * 1000 > startTimestamp }
                totalFound += filteredMessages.size

                for (message in filteredMessages) {
                    setProgressAsync(
                        Data.Builder()
                            .putInt("total", totalFound)
                            .putInt("downloaded", downloadedCount)
                            .putString("status", "Downloading...")
                            .build()
                    )

                    val content = message.content
                    val mediaItem: MediaItem? = when (content) {
                        is TdApi.MessagePhoto -> {
                            val photo = content.photo.sizes.last().photo
                            val file = telegramClient.downloadFile(photo.id)
                            MediaItem(
                                telegramMessageId = message.id,
                                filePath = file.local.path,
                                deviceModel = content.caption.text,
                                timestamp = message.date.toLong() * 1000,
                                mediaType = "photo"
                            )
                        }
                        is TdApi.MessageVideo -> {
                            val video = content.video.video
                            val file = telegramClient.downloadFile(video.id)
                            MediaItem(
                                telegramMessageId = message.id,
                                filePath = file.local.path,
                                deviceModel = content.caption.text,
                                timestamp = message.date.toLong() * 1000,
                                mediaType = "video"
                            )
                        }
                        else -> null
                    }

                    mediaItem?.let {
                        dao.insert(it)
                        downloadedCount++
                        if (it.timestamp > latestTimestamp) {
                            latestTimestamp = it.timestamp
                        }
                    }
                }
                fromMessageId = messages.messages.lastOrNull()?.id ?: 0
            } while (messages.messages.isNotEmpty() && (filteredMessages.size == messages.messages.size))

            if (latestTimestamp > lastSyncTimestamp) {
                sessionManager.saveLastMediaSyncTimestamp(latestTimestamp)
            }

            setProgressAsync(
                Data.Builder()
                    .putInt("total", totalFound)
                    .putInt("downloaded", downloadedCount)
                    .putString("status", "Completed")
                    .build()
            )
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
