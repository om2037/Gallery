package com.dark.cloud_gallery.domain.usecase

object CaptionParser {
    private val deviceModelRegex = Regex("\\[(.*?)\\]")

    fun parseDeviceModel(caption: String): String {
        return deviceModelRegex.find(caption)?.groups?.get(1)?.value ?: "Unknown"
    }
}
