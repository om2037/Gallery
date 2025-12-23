package com.dark.cloud_gallery.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileLogger private constructor(private val context: Context) {

    private val logFileName = "cloud_gallery_log.txt"

    companion object {
        const val AUTHORITY = "com.dark.cloud_gallery.fileprovider"

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
                ?: android.util.Log.e("FileLogger", "FileLogger not initialized.")
        }

        fun getLogFileUri(context: Context): Uri {
            val logFile = File(context.filesDir, instance?.logFileName ?: "cloud_gallery_log.txt")
            return FileProvider.getUriForFile(context, AUTHORITY, logFile)
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
            writeToInternalStorage(logText)
        } catch (e: Exception) {
            android.util.Log.e("FileLogger", "Failed to write to internal log file", e)
        }
    }

    private fun writeToInternalStorage(text: String) {
        try {
            val logFile = File(context.filesDir, logFileName)
            FileOutputStream(logFile, true).use {
                it.write(text.toByteArray())
            }
        } catch (e: IOException) {
            android.util.Log.e("FileLogger", "IOException while writing to internal log", e)
        }
    }
}
