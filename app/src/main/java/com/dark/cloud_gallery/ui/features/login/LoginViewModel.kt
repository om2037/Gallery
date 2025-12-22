package com.dark.cloud_gallery.ui.features.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dark.cloud_gallery.data.local.SessionManager
import com.dark.cloud_gallery.data.remote.TelegramClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val telegramClient: TelegramClient,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.WaitingForPhoneNumber)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // No need to initialize here, it will be done on demand
        }
    }

    fun sendAuthCode(apiId: String, apiHash: String, phoneNumber: String, channelId: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            telegramClient.initialize()
            sessionManager.saveApiCredentials(apiId, apiHash)
            sessionManager.saveChannelId(channelId)
            telegramClient.sendAuthenticationCode(apiId, apiHash, phoneNumber)
            // Start collecting the auth state flow AFTER initialization
            telegramClient.getAuthorizationStateFlow().collect {
                when (it) {
                    is TdApi.AuthorizationStateReady -> {
                        _uiState.value = LoginUiState.Success
                        sessionManager.setLoggedIn(true)
                    }

                    is TdApi.AuthorizationStateWaitCode -> _uiState.value =
                        LoginUiState.WaitingForCode

                    is TdApi.AuthorizationStateWaitPassword -> _uiState.value =
                        LoginUiState.WaitingForPassword

                    is TdApi.AuthorizationStateWaitPhoneNumber -> _uiState.value =
                        LoginUiState.WaitingForPhoneNumber

                    is TdApi.AuthorizationStateClosed -> _uiState.value =
                        LoginUiState.Error("Authentication failed")

                    else -> _uiState.value = LoginUiState.Loading
                }
            }
        }
    }

    fun checkAuthCode(code: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            telegramClient.checkAuthenticationCode(code)
        }
    }
}
