package com.dark.cloud_gallery.data.remote

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class TelegramClientImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : TelegramClient {

    private val client: Client = Client.create({ obj ->
        if (obj is TdApi.UpdateAuthorizationState) {
            _authorizationState.value = obj.authorizationState
        }
    }, null, null)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _authorizationState = MutableStateFlow<TdApi.AuthorizationState?>(null)

    init {
        client.send(TdApi.GetAuthorizationState()) {
            if (it is TdApi.AuthorizationState) {
                _authorizationState.value = it
            }
        }
    }

    override suspend fun sendAuthenticationCode(apiId: String, apiHash: String, phoneNumber: String) =
        suspendCancellableCoroutine<Unit> { continuation ->
            val apiIdInt = apiId.toIntOrNull()
            if (apiIdInt == null) {
                _authorizationState.value = TdApi.AuthorizationStateClosed()
                continuation.resume(Unit)
                return@suspendCancellableCoroutine
            }

            val params = TdApi.SetTdlibParameters()
            params.apiId = apiIdInt
            params.apiHash = apiHash
            params.databaseDirectory = context.filesDir.absolutePath
            params.useMessageDatabase = true
            params.useSecretChats = true
            params.systemLanguageCode = "en"
            params.deviceModel = "Android"
            params.systemVersion = "1"
            params.applicationVersion = "1.0"
            val setTdlibParameters = params
            client.send(setTdlibParameters) {
                when (it.constructor) {
                    TdApi.Ok.CONSTRUCTOR -> {
                        client.send(TdApi.SetAuthenticationPhoneNumber(phoneNumber, null)) { phoneResult ->
                            if (phoneResult.constructor == TdApi.Error.CONSTRUCTOR) {
                                _authorizationState.value = TdApi.AuthorizationStateClosed()
                            }
                            continuation.resume(Unit)
                        }
                    }
                    else -> {
                        _authorizationState.value = TdApi.AuthorizationStateClosed()
                        continuation.resume(Unit)
                    }
                }
            }
        }

    override suspend fun checkAuthenticationCode(code: String) {
        client.send(TdApi.CheckAuthenticationCode(code)) {
            if (it is TdApi.AuthorizationState) {
                _authorizationState.value = it
            }
        }
    }

    override suspend fun checkAuthenticationPassword(password: String) {
        client.send(TdApi.CheckAuthenticationPassword(password)) {
            if (it is TdApi.AuthorizationState) {
                _authorizationState.value = it
            }
        }
    }

    override fun getAuthorizationStateFlow(): Flow<TdApi.AuthorizationState> {
        return _authorizationState.asStateFlow().filter { it != null }.map { it!! }
    }
}
