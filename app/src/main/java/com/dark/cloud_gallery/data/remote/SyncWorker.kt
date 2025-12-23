package com.dark.cloud_gallery.data.remote

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.dark.cloud_gallery.di.SyncWorkerEntryPoint
import com.dark.cloud_gallery.domain.model.MediaItem
import com.dark.cloud_gallery.util.FileLogger
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.drinkless.tdlib.TdApi

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            SyncWorkerEntryPoint::class.java
        )
        val telegramClient = entryPoint.telegramClient()
        val sessionManager = entryPoint.sessionManager()
        val dao = entryPoint.mediaItemDao()

        FileLogger.log("SyncWorker", "Worker started.")
        try {
            val channelId = sessionManager.getChannelId()?.toLongOrNull() ?: run {
                FileLogger.log("SyncWorker", "Channel ID not found, stopping worker.")
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
                FileLogger.log("SyncWorker", "Fetching chat history from message ID: $fromMessageId")
                val messages = telegramClient.getChatHistory(channelId, fromMessageId)
                val filteredMessages = messages.messages.filter { it.date.toLong() * 1000 > startTimestamp }
                totalFound += filteredMessages.size
                FileLogger.log("SyncWorker", "Found ${messages.messages.size} messages, ${filteredMessages.size} are new.")

                for (message in filteredMessages) {
                    try {
                        FileLogger.log("SyncWorker", "Processing message ${message.id}")
                        setProgress(
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
                            FileLogger.log("SyncWorker", "Downloading and inserting media item for message ${message.id}")
                            dao.insert(it)
                            downloadedCount++
                            if (it.timestamp > latestTimestamp) {
                                latestTimestamp = it.timestamp
                            }
                        }
                    } catch (e: Exception) {
                        FileLogger.log("SyncWorker", "Failed to process message ${message.id}", e)
                        continue
                    }
                }
                fromMessageId = messages.messages.lastOrNull()?.id ?: 0
            } while (messages.messages.isNotEmpty() && (filteredMessages.size == messages.messages.size))

            if (latestTimestamp > lastSyncTimestamp) {
                FileLogger.log("SyncWorker", "Updating last sync timestamp to $latestTimestamp")
                sessionManager.saveLastMediaSyncTimestamp(latestTimestamp)
            }

            setProgress(
                Data.Builder()
                    .putInt("total", totalFound)
                    .putInt("downloaded", downloadedCount)
                    .putString("status", "Completed")
                    .build()
            )
            FileLogger.log("SyncWorker", "Sync completed successfully. Found $totalFound items, downloaded $downloadedCount new items.")
            Result.success()
        } catch (e: Exception) {
            FileLogger.log("SyncWorker", "Sync failed", e)
            Result.failure()
        }
    }
}
