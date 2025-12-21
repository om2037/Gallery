package com.dark.cloud_gallery.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dark.cloud_gallery.data.local.FileLogger
import com.dark.cloud_gallery.data.local.SessionManager
import com.dark.cloud_gallery.data.remote.TelegramClient
import com.dark.cloud_gallery.domain.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi
import javax.inject.Inject

import android.app.Application
@HiltViewModel
class MainViewModel @Inject constructor(
    private val telegramClient: TelegramClient,
    private val sessionManager: SessionManager,
    private val mediaRepository: MediaRepository,
    private val application: Application
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.LoggedIn)
    val authState = _authState.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError = _authError.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    init {
        checkAuthStatus()
    }

    fun checkAuthStatus() {
        FileLogger.log(application, "MainViewModel: Starting authentication status check.")
        _authError.value = null // Reset error state
        viewModelScope.launch {
            try {
                if (sessionManager.getApiId().isNullOrBlank() || sessionManager.getApiHash().isNullOrBlank()) {
                    FileLogger.log(application, "MainViewModel: No API ID or Hash found in SessionManager. Setting state to LoggedOut.")
                    _authState.value = AuthState.LoggedOut
                    return@launch
                }

                FileLogger.log(application, "MainViewModel: API credentials found. Subscribing to Telegram authorization state.")
                telegramClient.getAuthorizationStateFlow().collect {
                    FileLogger.log(application, "MainViewModel: Received new authorization state: ${it.javaClass.simpleName}")
                    when (it) {
                        is TdApi.AuthorizationStateReady -> {
                            _authState.value = AuthState.LoggedIn
                        }
                        is TdApi.AuthorizationStateWaitTdlibParameters,
                        is TdApi.AuthorizationStateWaitPhoneNumber,
                        is TdApi.AuthorizationStateWaitCode,
                        is TdApi.AuthorizationStateClosed -> {
                            _authState.value = AuthState.LoggedOut
                        }
                        // Potentially handle other states if needed, for now they do nothing
                    }
                }
            } catch (e: Exception) {
                FileLogger.log(application, "MainViewModel: Exception during auth check: ${e.message}")
                _authError.value = "Failed to check login status."
            }
        }
    }

    fun clearAuthError() {
        _authError.value = null
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
