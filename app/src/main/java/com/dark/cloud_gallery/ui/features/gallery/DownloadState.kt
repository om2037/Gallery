package com.dark.cloud_gallery.ui.features.gallery

sealed class DownloadState {
    object Idle : DownloadState()
    object InProgress : DownloadState()
    object Success : DownloadState()
    data class Error(val message: String) : DownloadState()
}
