package com.dark.cloud_gallery.data.local

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileLogger {

    private const val LOG_FILE_NAME = "cloud_gallery_log.txt"

    fun log(context: Context, message: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val logMessage = "$timestamp: $message\n"

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, LOG_FILE_NAME)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val queryUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
                val selection = "${MediaStore.MediaColumns.DISPLAY_NAME}=?"
                val selectionArgs = arrayOf(LOG_FILE_NAME)
                var logFileUri: Uri? = null

                resolver.query(queryUri, null, selection, selectionArgs, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                        logFileUri = Uri.withAppendedPath(queryUri, id.toString())
                    }
                }

                if (logFileUri == null) {
                    logFileUri = resolver.insert(queryUri, contentValues)
                }

                logFileUri?.let { uri ->
                    resolver.openOutputStream(uri, "wa")?.use { outputStream ->
                        outputStream.write(logMessage.toByteArray())
                    }
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }
                val logFile = File(downloadsDir, LOG_FILE_NAME)
                FileWriter(logFile, true).use { writer ->
                    writer.append(logMessage)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
