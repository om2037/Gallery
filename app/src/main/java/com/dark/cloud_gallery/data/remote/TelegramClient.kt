package com.dark.cloud_gallery.data.remote

import kotlinx.coroutines.flow.Flow
import org.drinkless.tdlib.TdApi

interface TelegramClient {
    suspend fun initialize()
    suspend fun sendAuthenticationCode(apiId: String, apiHash: String, phoneNumber: String)
    suspend fun checkAuthenticationCode(code: String)
    suspend fun checkAuthenticationPassword(password: String)
    fun getAuthorizationStateFlow(): Flow<TdApi.AuthorizationState>
    suspend fun getChatHistory(chatId: Long, fromMessageId: Long): TdApi.Messages
    suspend fun downloadFile(fileId: Int): TdApi.File
}
