package com.dark.cloud_gallery.ui

sealed class AuthState {
    object Loading : AuthState()
    object LoggedIn : AuthState()
    object LoggedOut : AuthState()
}
