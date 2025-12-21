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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private val _mediaItems = MutableStateFlow<List<MediaItem>>(emptyList())
    val mediaItems = _mediaItems.asStateFlow()

    private val _deviceModels = MutableStateFlow<List<String>>(emptyList())
    val deviceModels = _deviceModels.asStateFlow()

    private val _selectedDeviceModel = MutableStateFlow<String?>(null)
    val selectedDeviceModel = _selectedDeviceModel.asStateFlow()

    init {
        loadDeviceModels()
        viewModelScope.launch {
            _selectedDeviceModel.collect { deviceModel ->
                if (deviceModel == null) {
                    loadAllMediaItems()
                } else {
                    loadMediaItemsByDevice(deviceModel)
                }
            }
        }
    }

    private fun loadAllMediaItems() {
        mediaRepository.getAllMediaItems().onEach {
            _mediaItems.value = it
        }.launchIn(viewModelScope)
    }

    private fun loadMediaItemsByDevice(deviceModel: String) {
        mediaRepository.getMediaItemsByDevice(deviceModel).onEach {
            _mediaItems.value = it
        }.launchIn(viewModelScope)
    }

    private fun loadDeviceModels() {
        mediaRepository.getDeviceModels().onEach {
            _deviceModels.value = it
        }.launchIn(viewModelScope)
    }

    fun onDeviceModelSelected(deviceModel: String) {
        _selectedDeviceModel.value = if (_selectedDeviceModel.value == deviceModel) null else deviceModel
    }
}
