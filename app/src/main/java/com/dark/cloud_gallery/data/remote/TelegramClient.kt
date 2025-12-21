package com.dark.cloud_gallery.data.remote

import kotlinx.coroutines.flow.Flow
import org.drinkless.td.libcore.telegram.TdApi

interface TelegramClient {
    suspend fun sendAuthenticationCode(apiId: String, apiHash: String, phoneNumber: String)
    suspend fun checkAuthenticationCode(code: String)
    suspend fun checkAuthenticationPassword(password: String)
    fun getAuthorizationStateFlow(): Flow<TdApi.AuthorizationState>
}
