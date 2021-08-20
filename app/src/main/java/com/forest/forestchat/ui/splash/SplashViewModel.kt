/*
 * Copyright (C) 2021 Matthieu Bouquet <matthieu@forestchat.org>
 *
 * This file is part of ForestChat.
 *
 * ForestChat is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ForestChat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ForestChat.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.forest.forestchat.ui.splash

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import com.forest.forestchat.domain.useCases.SyncDataUseCase
import com.forest.forestchat.localStorage.sharedPrefs.LastSyncSharedPrefs
import com.forest.forestchat.manager.PermissionsManager
import com.forest.forestchat.ui.splash.models.SplashEvent
import com.zhuinden.eventemitter.EventEmitter
import com.zhuinden.eventemitter.EventSource
import com.zhuinden.livedatacombinetuplekt.combineTupleNonNull
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val syncDataUseCase: SyncDataUseCase,
    private val lastSyncSharedPrefs: LastSyncSharedPrefs,
    private val permissionsManager: PermissionsManager
) : ViewModel() {

    private val eventEmitter = EventEmitter<SplashEvent>()
    fun eventSource(): EventSource<SplashEvent> = eventEmitter

    private val syncDone = MutableLiveData(false)
    private val gdprReady = MutableLiveData(false)

    private val syncDataObserver = Observer<Boolean> { syncFinished ->
        if(syncFinished) {
            viewModelScope.launch(Dispatchers.IO) {
                syncDone.postValue(true)
            }
        }
    }

    init {
        syncDataUseCase.syncFinished().observeForever(syncDataObserver)

        combineTupleNonNull(syncDone, gdprReady)
            .distinctUntilChanged()
            .observeForever { (syncDone, gdprReady) ->
                if(syncDone && gdprReady) {
                    eventEmitter.emit(SplashEvent.GoToHome)
                }
            }
    }

    override fun onCleared() {
        syncDataUseCase.syncFinished().removeObserver(syncDataObserver)
    }

    fun syncDataIfNeeded() {
        viewModelScope.launch(Dispatchers.IO) {
            when {
                !permissionsManager.isDefaultSms() -> {
                    withContext(Dispatchers.Main) {
                        eventEmitter.emit(SplashEvent.RequestDefaultSms)
                    }
                }
                !permissionsManager.hasReadSms() || !permissionsManager.hasContacts() -> {
                    withContext(Dispatchers.Main) {
                        eventEmitter.emit(SplashEvent.RequestPermission)
                    }
                }
                else -> {
                    val lastSync = lastSyncSharedPrefs.get()
                    if (lastSync == 0L) {
                        syncDataUseCase()
                    } else {
                        syncDone.postValue(true)
                    }
                }
            }
        }
    }

    fun adsLoaded() {
        gdprReady.value = true
    }

}