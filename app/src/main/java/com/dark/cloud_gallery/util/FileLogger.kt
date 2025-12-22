package com.dark.cloud_gallery.util

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileLogger private constructor(private val context: Context) {

    private val logFileName = "2020.txt"

    companion object {
        @Volatile
        private var instance: FileLogger? = null

        fun initialize(context: Context) {
            synchronized(this) {
                if (instance == null) {
                    instance = FileLogger(context.applicationContext)
                }
            }
        }

        fun log(tag: String, message: String, throwable: Throwable? = null) {
            instance?.logInternal(tag, message, throwable)
                ?: android.util.Log.e("FileLogger", "FileLogger not initialized. Call initialize() first.")
        }
    }

    private fun logInternal(tag: String, message: String, throwable: Throwable? = null) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val logText = buildString {
            append("[$timestamp] $tag: $message\n")
            throwable?.let {
                append(it.stackTraceToString())
                append("\n")
            }
        }

        try {
            writeToDownloads(logText)
        } catch (e: Exception) {
            android.util.Log.e("FileLogger", "Failed to write to log file", e)
        }
    }

    private fun writeToDownloads(text: String) {
        try {
            @Suppress("DEPRECATION")
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            val logFile = File(downloadsDir, logFileName)
            FileOutputStream(logFile, true).use {
                it.write(text.toByteArray())
            }
        } catch (e: IOException) {
            android.util.Log.e("FileLogger", "IOException while writing to log file", e)
        }
    }
}
