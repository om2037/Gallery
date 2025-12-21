package com.dark.cloud_gallery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import com.dark.cloud_gallery.ui.MainViewModel
import com.dark.cloud_gallery.ui.Navigation
import com.dark.cloud_gallery.ui.theme.CloudGalleryTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CloudGalleryTheme {
                Navigation(viewModel = viewModel)
            }
            LaunchedEffect(Unit) {
                viewModel.checkInitialState()
            }
        }
    }
}
