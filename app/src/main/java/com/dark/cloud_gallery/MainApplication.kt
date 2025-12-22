package com.dark.cloud_gallery

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.dark.cloud_gallery.data.remote.SyncWorker
import com.dark.cloud_gallery.util.FileLogger
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application() {

    @Inject
    lateinit var fileLogger: FileLogger

    override fun onCreate() {
        super.onCreate()
        fileLogger.log("MainApplication", "Application starting up.")
        setupPeriodicSync()
    }

    private fun setupPeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(30, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "periodic_sync_work",
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )
    }
}
