package com.dark.cloud_gallery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.dark.cloud_gallery.ui.Navigation
import com.dark.cloud_gallery.ui.theme.CloudGalleryTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CloudGalleryTheme {
                Navigation()
            }
        }
    }
}
