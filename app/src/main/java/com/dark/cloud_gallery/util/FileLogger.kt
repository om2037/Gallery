package com.dark.cloud_gallery.util

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileLogger @Inject constructor(@ApplicationContext private val context: Context) {

    private val logFileName = "2020.txt"

    fun log(tag: String, message: String, throwable: Throwable? = null) {
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
            // Fallback or log to Logcat if file writing fails
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

            // Try to find an existing file to append to it
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
                    outputStream = resolver.openOutputStream(uri, "wa") // "wa" is for write-append
                }
            }

            // If file doesn't exist, create it
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
            // For older Android versions
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
