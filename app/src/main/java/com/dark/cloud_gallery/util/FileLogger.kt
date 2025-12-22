package com.dark.cloud_gallery.util

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
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
            // Ensure this can be called from any thread
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
        } catch (e: IOException) {
            android.util.Log.e("FileLogger", "Failed to write to log file", e)
        }
    }

    private fun writeToDownloads(text: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, logFileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val queryUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
            val selection = "${MediaStore.MediaColumns.RELATIVE_PATH} = ? AND ${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
            val selectionArgs = arrayOf("${Environment.DIRECTORY_DOWNLOADS}/", logFileName)
            var outputStream: OutputStream? = null

            resolver.query(queryUri, null, selection, selectionArgs, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val uri = cursor.run {
                        val idColumn = getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                        val id = getLong(idColumn)
                        android.net.Uri.withAppendedPath(queryUri, id.toString())
                    }
                    outputStream = resolver.openOutputStream(uri, "wa") // "wa" for write-append
                }
            }

            if (outputStream == null) {
                val newUri = resolver.insert(queryUri, contentValues)
                if (newUri != null) {
                    outputStream = resolver.openOutputStream(newUri, "w")
                }
            }

            outputStream?.use {
                it.write(text.toByteArray())
            }

        } else {
            @Suppress("DEPRECATION")
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            val logFile = File(downloadsDir, logFileName)
            FileOutputStream(logFile, true).use {
                it.write(text.toByteArray())
            }
        }
    }
}
