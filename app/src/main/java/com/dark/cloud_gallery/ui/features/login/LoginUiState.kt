package com.dark.cloud_gallery.ui.features.login

sealed class LoginUiState {
    object WaitingForPhoneNumber : LoginUiState()
    object WaitingForCode : LoginUiState()
    object WaitingForPassword : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
