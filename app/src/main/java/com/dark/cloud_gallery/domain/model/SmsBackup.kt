package com.dark.cloud_gallery.domain.model

data class SmsBackup(
    val id: Long,
    val filePath: String,
    val deviceModel: String,
    val timestamp: Long
)
