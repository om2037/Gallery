package com.dark.cloud_gallery.ui.features.gallery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage

@Composable
fun GalleryScreen(
    navController: NavController,
    viewModel: GalleryViewModel = hiltViewModel()
) {
    val groupedMediaItems by viewModel.groupedMediaItems.collectAsState()
    val sortedDates = groupedMediaItems.keys.sortedDescending()

    LazyColumn(modifier = Modifier.padding(8.dp)) {
        items(sortedDates.size) { index ->
            val date = sortedDates[index]
            val itemsForDate = groupedMediaItems[date] ?: emptyList()

            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Text(
                    text = date,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 100.dp),
                    modifier = Modifier.padding(horizontal = 4.dp),
                    userScrollEnabled = false // Important for nested scrolling
                ) {
                    items(itemsForDate) { item ->
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
    }
}
