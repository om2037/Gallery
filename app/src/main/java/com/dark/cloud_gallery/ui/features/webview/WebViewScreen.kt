package com.dark.cloud_gallery.ui.features.webview

import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun WebViewScreen(url: String) {
    AndroidView(factory = { context ->
        WebView(context).apply {
            loadUrl(url)
        }
    })
}
