package com.dark.cloud_gallery.ui.features.gallery

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDetailScreen(
    viewModel: MediaDetailViewModel = hiltViewModel()
) {
    val mediaItem by viewModel.mediaItem.collectAsState()
    val isBottomSheetVisible by viewModel.isBottomSheetVisible.collectAsState()

    mediaItem?.let { item ->
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = item.filePath,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )

            IconButton(
                onClick = { viewModel.showBottomSheet() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Icon(Icons.Filled.Info, contentDescription = "Show Info")
            }

            if (isBottomSheetVisible) {
                ModalBottomSheet(onDismissRequest = { viewModel.hideBottomSheet() }) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Captured on ${item.deviceModel}")
                        Text("Date: ${item.timestamp}")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.deleteMediaItem() },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
        }
    }
}
