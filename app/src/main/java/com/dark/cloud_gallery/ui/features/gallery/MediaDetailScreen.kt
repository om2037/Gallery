package com.dark.cloud_gallery.ui.features.gallery

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MediaDetailScreen(
    viewModel: MediaDetailViewModel = hiltViewModel()
) {
    val mediaItem by viewModel.mediaItem.collectAsState()
    val isBottomSheetVisible by viewModel.isBottomSheetVisible.collectAsState()
    val storagePermissionState = rememberPermissionState(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    val downloadState by viewModel.downloadState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val zoomState = rememberZoomState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        mediaItem?.let { item ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                AsyncImage(
                    model = "file://${item.filePath}",
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().zoomable(zoomState)
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
                                onClick = {
                                    if (storagePermissionState.status.isGranted) {
                                        viewModel.downloadMediaItem()
                                    } else {
                                        storagePermissionState.launchPermissionRequest()
                                    }
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(Icons.Filled.Download, contentDescription = "Download")
                            }
                            Button(
                                onClick = { viewModel.deleteMediaItem() },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }

                LaunchedEffect(downloadState) {
                    when (downloadState) {
                        is DownloadState.Success -> snackbarHostState.showSnackbar("Downloaded successfully")
                        is DownloadState.Error -> snackbarHostState.showSnackbar("Download failed")
                        else -> {}
                    }
                }

                if (downloadState is DownloadState.InProgress) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}
