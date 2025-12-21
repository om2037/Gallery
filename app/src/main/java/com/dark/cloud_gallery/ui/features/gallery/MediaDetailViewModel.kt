package com.dark.cloud_gallery.ui.features.gallery

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dark.cloud_gallery.domain.model.MediaItem
import com.dark.cloud_gallery.domain.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MediaDetailViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val mediaId: Long = checkNotNull(savedStateHandle["mediaId"])

    private val _mediaItem = MutableStateFlow<MediaItem?>(null)
    val mediaItem = _mediaItem.asStateFlow()

    private val _isBottomSheetVisible = MutableStateFlow(false)
    val isBottomSheetVisible = _isBottomSheetVisible.asStateFlow()

    init {
        loadMediaItem()
    }

    private fun loadMediaItem() {
        viewModelScope.launch {
            _mediaItem.value = mediaRepository.getMediaItemById(mediaId)
        }
    }

    fun deleteMediaItem() {
        viewModelScope.launch {
            _mediaItem.value?.let {
                // In a real app, you would also delete the file from storage
                mediaRepository.updateMediaItem(it.copy(isDeleted = true))
            }
        }
    }

    fun showBottomSheet() {
        _isBottomSheetVisible.value = true
    }

    fun hideBottomSheet() {
        _isBottomSheetVisible.value = false
    }
}
