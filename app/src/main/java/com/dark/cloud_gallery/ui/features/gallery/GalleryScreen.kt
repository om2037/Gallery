package com.dark.cloud_gallery.ui.features.gallery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    navController: NavController,
    viewModel: GalleryViewModel = hiltViewModel()
) {
    val mediaItems by viewModel.mediaItems.collectAsState()
    val deviceModels by viewModel.deviceModels.collectAsState()
    val selectedDeviceModel by viewModel.selectedDeviceModel.collectAsState()

    Column {
        FilterChips(
            deviceModels = deviceModels,
            selectedDeviceModel = selectedDeviceModel,
            onDeviceSelected = viewModel::onDeviceModelSelected
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 128.dp),
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            items(mediaItems) { item ->
                AsyncImage(
                    model = item.filePath,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable {
                            navController.navigate("mediaDetail/${item.id}")
                        }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChips(
    deviceModels: List<String>,
    selectedDeviceModel: String?,
    onDeviceSelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.padding(8.dp)
    ) {
        items(deviceModels) { deviceModel ->
            FilterChip(
                modifier = Modifier.padding(horizontal = 4.dp),
                selected = deviceModel == selectedDeviceModel,
                onClick = { onDeviceSelected(deviceModel) },
                label = { Text(deviceModel) }
            )
        }
    }
}
