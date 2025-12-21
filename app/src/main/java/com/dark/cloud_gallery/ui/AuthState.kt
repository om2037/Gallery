package com.dark.cloud_gallery.ui

sealed class AuthState {
    object LoggedIn : AuthState()
    object LoggedOut : AuthState()
}
