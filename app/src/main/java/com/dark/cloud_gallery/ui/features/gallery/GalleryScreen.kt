package com.dark.cloud_gallery.ui.features.gallery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
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

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 100.dp),
        modifier = Modifier.padding(8.dp),
    ) {
        sortedDates.forEach { date ->
            val itemsForDate = groupedMediaItems[date] ?: emptyList()

            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = date,
                    modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 8.dp)
                )
            }

            items(itemsForDate) { item ->
                AsyncImage(
                    model = "file://${item.filePath}",
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clickable {
                            navController.navigate("mediaDetail/${item.id}")
                        }
                )
            }
        }
    }
}
