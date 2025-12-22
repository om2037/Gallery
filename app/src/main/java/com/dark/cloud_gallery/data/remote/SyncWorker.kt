package com.dark.cloud_gallery.data.remote

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.dark.cloud_gallery.data.local.MediaItemDao
import com.dark.cloud_gallery.data.local.SessionManager
import com.dark.cloud_gallery.domain.model.MediaItem
import com.dark.cloud_gallery.util.FileLogger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.drinkless.tdlib.TdApi

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val telegramClient: TelegramClient,
    private val sessionManager: SessionManager,
    private val dao: MediaItemDao,
    private val fileLogger: FileLogger
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        fileLogger.log("SyncWorker", "Worker started.")
        try {
            val channelId = sessionManager.getChannelId()?.toLongOrNull() ?: run {
                fileLogger.log("SyncWorker", "Channel ID not found, stopping worker.")
                return@withContext Result.failure()
            }
            val lastSyncTimestamp = sessionManager.getLastMediaSyncTimestamp()
            val syncStartDate = sessionManager.getSyncStartDate()
            val startTimestamp = if (lastSyncTimestamp > 0) lastSyncTimestamp else syncStartDate

            var fromMessageId: Long = 0
            var latestTimestamp = lastSyncTimestamp
            var totalFound = 0
            var downloadedCount = 0

            do {
                fileLogger.log("SyncWorker", "Fetching chat history from message ID: $fromMessageId")
                val messages = telegramClient.getChatHistory(channelId, fromMessageId)
                val filteredMessages = messages.messages.filter { it.date.toLong() * 1000 > startTimestamp }
                totalFound += filteredMessages.size
                fileLogger.log("SyncWorker", "Found ${messages.messages.size} messages, ${filteredMessages.size} are new.")

                for (message in filteredMessages) {
                    try {
                        fileLogger.log("SyncWorker", "Processing message ${message.id}")
                        setProgressAsync(
                            Data.Builder()
                                .putInt("total", totalFound)
                                .putInt("downloaded", downloadedCount)
                                .putString("status", "Downloading...")
                                .build()
                        )

                        val content = message.content
                        val captionText = (when (content) {
                            is TdApi.MessagePhoto -> content.caption.text
                            is TdApi.MessageVideo -> content.caption.text
                            else -> ""
                        }).ifBlank { "Unknown Device" }


                        val mediaItem: MediaItem? = when (content) {
                            is TdApi.MessagePhoto -> {
                                val photo = content.photo.sizes.maxByOrNull { it.width * it.height }?.photo ?: continue
                                val file = telegramClient.downloadFile(photo.id)
                                MediaItem(
                                    telegramMessageId = message.id,
                                    filePath = file.local.path,
                                    deviceModel = captionText,
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
                                    deviceModel = captionText,
                                    timestamp = message.date.toLong() * 1000,
                                    mediaType = "video"
                                )
                            }
                            else -> null
                        }

                        mediaItem?.let {
                            fileLogger.log("SyncWorker", "Downloading and inserting media item for message ${message.id}")
                            dao.insert(it)
                            downloadedCount++
                            if (it.timestamp > latestTimestamp) {
                                latestTimestamp = it.timestamp
                            }
                        }
                    } catch (e: Exception) {
                        fileLogger.log("SyncWorker", "Failed to process message ${message.id}", e)
                        continue
                    }
                }
                fromMessageId = messages.messages.lastOrNull()?.id ?: 0
            } while (messages.messages.isNotEmpty() && (filteredMessages.size == messages.messages.size))

            if (latestTimestamp > lastSyncTimestamp) {
                fileLogger.log("SyncWorker", "Updating last sync timestamp to $latestTimestamp")
                sessionManager.saveLastMediaSyncTimestamp(latestTimestamp)
            }

            setProgressAsync(
                Data.Builder()
                    .putInt("total", totalFound)
                    .putInt("downloaded", downloadedCount)
                    .putString("status", "Completed")
                    .build()
            )
            fileLogger.log("SyncWorker", "Sync completed successfully. Found $totalFound items, downloaded $downloadedCount new items.")
            Result.success()
        } catch (e: Exception) {
            fileLogger.log("SyncWorker", "Sync failed", e)
            Result.failure()
        }
    }
}
