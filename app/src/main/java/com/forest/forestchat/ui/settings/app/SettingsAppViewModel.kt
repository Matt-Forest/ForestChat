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
package com.forest.forestchat.ui.settings.app

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forest.forestchat.R
import com.forest.forestchat.domain.useCases.SyncDataUseCase
import com.forest.forestchat.manager.PermissionsManager
import com.forest.forestchat.ui.conversations.models.HomeConversationsState
import com.forest.forestchat.ui.settings.app.models.SettingsAppEvent
import com.zhuinden.eventemitter.EventEmitter
import com.zhuinden.eventemitter.EventSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsAppViewModel @Inject constructor(
    private val syncDataUseCase: SyncDataUseCase,
    private val permissionsManager: PermissionsManager
) : ViewModel() {

    private val eventEmitter = EventEmitter<SettingsAppEvent>()
    fun eventSource(): EventSource<SettingsAppEvent> = eventEmitter

    private val loading = MutableLiveData<Boolean>()
    fun loading(): LiveData<Boolean> = loading

    private val syncDataObserver = Observer<Boolean> { syncFinished ->
        if(syncFinished) {
            viewModelScope.launch(Dispatchers.IO) {
                loading.postValue(false)
            }
        }
    }

    init {
        syncDataUseCase.syncFinished().observeForever(syncDataObserver)
    }

    fun syncData() {
        viewModelScope.launch(Dispatchers.IO) {
            loading.postValue(true)
            when {
                !permissionsManager.isDefaultSms() -> {
                    withContext(Dispatchers.Main) {
                        eventEmitter.emit(SettingsAppEvent.RequestDefaultSms)
                    }
                }
                !permissionsManager.hasReadSms() || !permissionsManager.hasContacts() -> {
                    withContext(Dispatchers.Main) {
                        eventEmitter.emit(SettingsAppEvent.RequestPermission)
                    }
                }
                else -> syncDataUseCase()
            }
        }
    }

    override fun onCleared() {
        syncDataUseCase.syncFinished().removeObserver(syncDataObserver)
    }

}