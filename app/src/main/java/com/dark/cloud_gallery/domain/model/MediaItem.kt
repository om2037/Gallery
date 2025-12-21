package com.dark.cloud_gallery.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_items")
data class MediaItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val filePath: String,
    val telegramMessageId: Long,
    val deviceModel: String,
    val timestamp: Long,
    val mediaType: String,
    val isDeleted: Boolean = false
)
