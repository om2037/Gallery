package com.dark.cloud_gallery.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dark.cloud_gallery.data.local.SessionManager
import com.dark.cloud_gallery.data.remote.TelegramClient
import com.dark.cloud_gallery.domain.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val telegramClient: TelegramClient,
    private val sessionManager: SessionManager,
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState = _authState.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    init {
        viewModelScope.launch {
            // First, check if we have the necessary credentials to even attempt to connect
            if (sessionManager.getApiId().isNullOrBlank() || sessionManager.getApiHash().isNullOrBlank()) {
                _authState.value = AuthState.LoggedOut
            } else {
                // If we have credentials, then we can proceed to check the Telegram client's state
                telegramClient.getAuthorizationStateFlow().collect {
                    when (it) {
                        is TdApi.AuthorizationStateReady -> _authState.value = AuthState.LoggedIn
                        is TdApi.AuthorizationStateWaitPhoneNumber,
                        is TdApi.AuthorizationStateWaitCode,
                        is TdApi.AuthorizationStateClosed -> _authState.value = AuthState.LoggedOut
                        // Keep loading during other intermediate states like WaitTdlibParameters
                        is TdApi.AuthorizationStateWaitTdlibParameters -> {} // Do nothing, stay in loading
                        else -> {
                            // For any other unhandled state, do nothing to avoid incorrectly logging out
                        }
                    }
                }
            }
        }
    }

    fun saveSyncStartDate(dateMillis: Long?) {
        dateMillis?.let {
            sessionManager.saveSyncStartDate(it)
            triggerSync()
        }
    }

    private fun triggerSync() {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                mediaRepository.syncMediaItems()
                // Also refresh SMS backups, though it's a flow that should update automatically
                // Forcing a refresh could be done here if needed
            } finally {
                _isSyncing.value = false
            }
        }
    }
}
