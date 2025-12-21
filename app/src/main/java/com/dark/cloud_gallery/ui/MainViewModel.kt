package com.dark.cloud_gallery.ui

import androidx.lifecycle.ViewModel
import com.dark.cloud_gallery.data.local.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {
    fun isLoggedIn(): Boolean {
        return sessionManager.isLoggedIn()
    }
}
