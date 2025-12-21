package com.dark.cloud_gallery.ui.features.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dark.cloud_gallery.domain.model.MediaItem
import com.dark.cloud_gallery.domain.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private val _groupedMediaItems = MutableStateFlow<Map<String, List<MediaItem>>>(emptyMap())
    val groupedMediaItems = _groupedMediaItems.asStateFlow()

    init {
        loadAllMediaItems()
    }

    private fun loadAllMediaItems() {
        mediaRepository.getAllMediaItems().onEach { items ->
            _groupedMediaItems.value = items.groupBy {
                formatTimestampToDateString(it.timestamp)
            }
        }.launchIn(viewModelScope)
    }

    private fun formatTimestampToDateString(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
