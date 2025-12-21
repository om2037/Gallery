package com.dark.cloud_gallery.ui.features.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dark.cloud_gallery.data.local.FileLogger
import com.dark.cloud_gallery.data.local.SessionManager
import com.dark.cloud_gallery.data.remote.TelegramClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi
import javax.inject.Inject

import android.app.Application
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val telegramClient: TelegramClient,
    private val sessionManager: SessionManager,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.WaitingForPhoneNumber)
    val uiState = _uiState.asStateFlow()

    init {
        FileLogger.log(application, "LoginViewModel: Initializing and subscribing to authorization state.")
        viewModelScope.launch {
            telegramClient.getAuthorizationStateFlow().collect {
                FileLogger.log(application, "LoginViewModel: Received new authorization state: ${it.javaClass.simpleName}")
                when (it) {
                    is TdApi.AuthorizationStateReady -> {
                        _uiState.value = LoginUiState.Success
                        sessionManager.setLoggedIn(true)
                        FileLogger.log(application, "LoginViewModel: State is Ready. Login successful.")
                    }
                    is TdApi.AuthorizationStateWaitCode -> _uiState.value = LoginUiState.WaitingForCode
                    is TdApi.AuthorizationStateWaitPassword -> _uiState.value = LoginUiState.WaitingForPassword
                    is TdApi.AuthorizationStateWaitPhoneNumber,
                    is TdApi.AuthorizationStateWaitTdlibParameters -> _uiState.value = LoginUiState.WaitingForPhoneNumber
                    is TdApi.AuthorizationStateClosed -> {
                        sessionManager.setLoggedIn(false)
                        _uiState.value = LoginUiState.Error("Authentication failed")
                        FileLogger.log(application, "LoginViewModel: State is Closed. Authentication failed.")
                    }
                    else -> _uiState.value = LoginUiState.Loading
                }
            }
        }
    }

    fun sendAuthCode(apiId: String, apiHash: String, phoneNumber: String, channelId: String) {
        FileLogger.log(application, "LoginViewModel: sendAuthCode called for phone number: $phoneNumber")
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                sessionManager.saveApiCredentials(apiId, apiHash)
                sessionManager.saveChannelId(channelId)
                telegramClient.sendAuthenticationCode(apiId, apiHash, phoneNumber)
                FileLogger.log(application, "LoginViewModel: Authentication code sent successfully.")
            } catch (e: Exception) {
                FileLogger.log(application, "LoginViewModel: Error sending auth code: ${e.message}")
                _uiState.value = LoginUiState.Error("Failed to send code: ${e.message}")
            }
        }
    }

    fun checkAuthCode(code: String) {
        FileLogger.log(application, "LoginViewModel: checkAuthCode called with code.")
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                telegramClient.checkAuthenticationCode(code)
                FileLogger.log(application, "LoginViewModel: Authentication code checked successfully.")
            } catch (e: Exception) {
                FileLogger.log(application, "LoginViewModel: Error checking auth code: ${e.message}")
                _uiState.value = LoginUiState.Error("Failed to check code: ${e.message}")
            }
        }
    }
}
