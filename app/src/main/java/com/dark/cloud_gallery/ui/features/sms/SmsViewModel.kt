package com.dark.cloud_gallery.ui.features.sms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dark.cloud_gallery.domain.model.SmsBackup
import com.dark.cloud_gallery.domain.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SmsViewModel @Inject constructor(
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private val _smsBackups = MutableStateFlow<List<SmsBackup>>(emptyList())
    val smsBackups = _smsBackups.asStateFlow()

    init {
        viewModelScope.launch {
            mediaRepository.getSmsBackups().collect {
                _smsBackups.value = it
            }
        }
    }
}
