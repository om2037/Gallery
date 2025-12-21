package com.dark.cloud_gallery.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dark.cloud_gallery.data.remote.TelegramClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val telegramClient: TelegramClient
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState = _authState.asStateFlow()

    init {
        viewModelScope.launch {
            telegramClient.getAuthorizationStateFlow().collect {
                when (it) {
                    is TdApi.AuthorizationStateReady -> _authState.value = AuthState.LoggedIn
                    is TdApi.AuthorizationStateWaitPhoneNumber -> _authState.value = AuthState.LoggedOut
                    is TdApi.AuthorizationStateClosed -> _authState.value = AuthState.LoggedOut
                }
            }
        }
    }
}
