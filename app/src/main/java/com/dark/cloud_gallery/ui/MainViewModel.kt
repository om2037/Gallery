package com.dark.cloud_gallery.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.dark.cloud_gallery.data.local.SessionManager
import com.dark.cloud_gallery.data.remote.TelegramClient
import com.dark.cloud_gallery.domain.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import com.dark.cloud_gallery.util.Constants.SYNC_WORK_TAG
import com.dark.cloud_gallery.util.FileLogger
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi
import javax.inject.Inject

data class SyncProgress(
    val total: Int = 0,
    val downloaded: Int = 0,
    val status: String = ""
)

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val telegramClient: TelegramClient,
    private val sessionManager: SessionManager,
    private val mediaRepository: MediaRepository,
    private val fileLogger: FileLogger
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState = _authState.asStateFlow()

    private val _syncProgress = MutableStateFlow<SyncProgress?>(null)
    val syncProgress = _syncProgress.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.loggedInStateFlow.collect { isLoggedIn ->
                if (isLoggedIn) {
                    _authState.value = AuthState.LoggedIn
                    checkInitialState()
                } else {
                    _authState.value = AuthState.LoggedOut
                }
            }
        }
    }

    fun checkInitialState() {
        viewModelScope.launch {
            if (sessionManager.isLoggedIn()) {
                telegramClient.initialize()
                telegramClient.getAuthorizationStateFlow().collect {
                    if (it is TdApi.AuthorizationStateReady) {
                        _authState.value = AuthState.LoggedIn
                    } else if (it is TdApi.AuthorizationStateClosed) {
                        _authState.value = AuthState.LoggedOut
                        sessionManager.setLoggedIn(false)
                    }
                }
            }
        }
    }

    fun saveSyncStartDate(dateMillis: Long?) {
        fileLogger.log("MainViewModel", "Sync from date button clicked.")
        dateMillis?.let {
            fileLogger.log("MainViewModel", "Date selected: $it. Triggering sync.")
            sessionManager.saveSyncStartDate(it)
            triggerSync()
        }
    }

    fun triggerImmediateSync() {
        fileLogger.log("MainViewModel", "Immediate sync button clicked. Triggering sync.")
        triggerSync()
    }

    private val workManager = WorkManager.getInstance(context)
    private val workInfosObserver = Observer<List<WorkInfo>> { workInfos ->
        val workInfo = workInfos.firstOrNull() ?: return@Observer
        if (workInfo.state.isFinished) {
            _syncProgress.value = null
        } else {
            val progress = workInfo.progress
            _syncProgress.value = SyncProgress(
                total = progress.getInt("total", 0),
                downloaded = progress.getInt("downloaded", 0),
                status = progress.getString("status") ?: ""
            )
        }
    }

    private fun triggerSync() {
        fileLogger.log("MainViewModel", "Entering triggerSync().")
        viewModelScope.launch {
            fileLogger.log("MainViewModel", "Coroutine for sync started. Calling mediaRepository.syncMediaItems().")
            mediaRepository.syncMediaItems()
            fileLogger.log("MainViewModel", "Returned from syncMediaItems. Now observing WorkManager LiveData.")
            workManager.getWorkInfosByTagLiveData(SYNC_WORK_TAG)
                .observeForever(workInfosObserver)
        }
    }

    override fun onCleared() {
        super.onCleared()
        workManager.getWorkInfosByTagLiveData(SYNC_WORK_TAG)
            .removeObserver(workInfosObserver)
    }
}
