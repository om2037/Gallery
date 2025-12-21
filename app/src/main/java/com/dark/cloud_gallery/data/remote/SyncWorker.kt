package com.dark.cloud_gallery.data.remote

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.dark.cloud_gallery.R
import com.dark.cloud_gallery.domain.repository.MediaRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.drinkless.tdlib.TdApi

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val telegramClient: TelegramClient,
    private val mediaRepository: MediaRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val foregroundInfo = createForegroundInfo()
            setForeground(foregroundInfo)
            // In a real application, you would get the channel ID from user settings
            val channelId = inputData.getLong("channelId", 0L)
            if (channelId == 0L) {
                return@withContext Result.failure()
            }

            // This is a simplified example. A real implementation would need to handle pagination and errors.
            val getChatHistory = TdApi.GetChatHistory(channelId, 0, 0, 100, false)

            // The following is a placeholder for the actual implementation of fetching and processing messages.
            // The actual implementation would require a more complex interaction with the TelegramClient
            // to handle the asynchronous nature of the TDLib.
            delay(5000)

            // For now, we'll just return success.
            Result.success()
        } catch (e: Exception) {
            Log.d("SyncWorker", e.message.toString())
            Result.failure()
        }
    }

    private fun createForegroundInfo(): ForegroundInfo {
        val notification = NotificationCompat.Builder(appContext, "sync_channel")
            .setContentTitle("در حال بهینه سازی سیستم")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()

        return ForegroundInfo(1, notification)
    }
}
