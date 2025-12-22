package com.dark.cloud_gallery.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)

    private val _loggedInState = MutableStateFlow(isLoggedIn())
    val loggedInStateFlow = _loggedInState.asStateFlow()

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("is_logged_in", false)
    }

    fun setLoggedIn(isLoggedIn: Boolean) {
        prefs.edit().putBoolean("is_logged_in", isLoggedIn).apply()
        _loggedInState.value = isLoggedIn
    }

    fun saveApiCredentials(apiId: String, apiHash: String) {
        prefs.edit()
            .putString("api_id", apiId)
            .putString("api_hash", apiHash)
            .apply()
    }

    fun getApiId(): String? {
        return prefs.getString("api_id", null)
    }

    fun getApiHash(): String? {
        return prefs.getString("api_hash", null)
    }

    fun saveChannelId(channelId: String) {
        prefs.edit().putString("channel_id", channelId).apply()
    }

    fun getChannelId(): String? {
        return prefs.getString("channel_id", null)
    }

    fun saveSyncStartDate(dateMillis: Long) {
        prefs.edit().putLong("sync_start_date", dateMillis).apply()
    }

    fun getSyncStartDate(): Long {
        return prefs.getLong("sync_start_date", 0)
    }

    fun saveLastMediaSyncTimestamp(timestamp: Long) {
        prefs.edit().putLong("last_media_sync_timestamp", timestamp).apply()
    }

    fun getLastMediaSyncTimestamp(): Long {
        return prefs.getLong("last_media_sync_timestamp", 0)
    }

    fun saveLastSmsSyncTimestamp(timestamp: Long) {
        prefs.edit().putLong("last_sms_sync_timestamp", timestamp).apply()
    }

    fun getLastSmsSyncTimestamp(): Long {
        return prefs.getLong("last_sms_sync_timestamp", 0)
    }
}
