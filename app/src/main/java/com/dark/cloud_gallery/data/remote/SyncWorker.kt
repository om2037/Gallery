package com.dark.cloud_gallery.data.remote

import android.app.Notification
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.dark.cloud_gallery.R
import com.dark.cloud_gallery.di.SyncWorkerEntryPoint
import com.dark.cloud_gallery.domain.model.MediaItem
import com.dark.cloud_gallery.util.FileLogger
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.drinkless.tdlib.TdApi
import java.util.UUID

class SyncWorker(
    private val appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "sync_channel_id"
    }

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
            setForeground(createForegroundInfo("Starting sync..."))
            telegramClient.initialize()

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

            while (true) {
                FileLogger.log("SyncWorker", "Fetching chat history from message ID: $fromMessageId")
                setForeground(createForegroundInfo("Fetching messages... Found: $totalFound", downloadedCount, totalFound))

                val messages = telegramClient.getChatHistory(channelId, fromMessageId)
                if (messages.messages.isEmpty()) {
                    FileLogger.log("SyncWorker", "No more messages found. Exiting loop.")
                    break
                }

                val newMessages = messages.messages.filter { it.date.toLong() * 1000 > startTimestamp }
                totalFound += newMessages.size

                FileLogger.log("SyncWorker", "Found ${messages.messages.size} total messages, ${newMessages.size} are new.")

                for (message in newMessages) {
                    try {
                        FileLogger.log("SyncWorker", "Processing message ${message.id}")
                        val statusMessage = "Downloading ${downloadedCount + 1} of $totalFound..."
                        setForeground(createForegroundInfo(statusMessage, downloadedCount + 1, totalFound))
                        setProgress(
                            Data.Builder()
                                .putInt("total", totalFound)
                                .putInt("downloaded", downloadedCount)
                                .putString("status", statusMessage)
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
                                FileLogger.log("SyncWorker", "File downloaded to: ${file.local.path}")
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
                                FileLogger.log("SyncWorker", "File downloaded to: ${file.local.path}")
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
                            FileLogger.log("SyncWorker", "Inserting media item for message ${message.id}")
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
                if (fromMessageId == 0L || newMessages.size < messages.messages.size) {
                    FileLogger.log("SyncWorker", "Reached end of new messages. Exiting loop.")
                    break
                }
            }

            if (latestTimestamp > lastSyncTimestamp) {
                FileLogger.log("SyncWorker", "Updating last sync timestamp to $latestTimestamp")
                sessionManager.saveLastMediaSyncTimestamp(latestTimestamp)
            }

            val finalStatus = "Sync completed. Downloaded $downloadedCount new items."
            setForeground(createForegroundInfo(finalStatus))
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

    private fun createForegroundInfo(progress: String, downloaded: Int = 0, total: Int = 0): ForegroundInfo {
        val title = "Syncing Media"
        val notificationBuilder = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(progress)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)

        if (total > 0) {
            notificationBuilder.setProgress(total, downloaded, false)
        }

        val notification = notificationBuilder.build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }
}
