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
package com.forest.forestchat.ui.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forest.forestchat.app.PermissionsManager
import com.forest.forestchat.domain.useCases.GetConversationsUseCase
import com.forest.forestchat.domain.useCases.synchronize.SyncDataUseCase
import com.forest.forestchat.localStorage.sharedPrefs.LastSyncSharedPrefs
import com.zhuinden.eventemitter.EventEmitter
import com.zhuinden.eventemitter.EventSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val getConversationsUseCase: GetConversationsUseCase,
    private val syncDataUseCase: SyncDataUseCase,
    private val lastSyncSharedPrefs: LastSyncSharedPrefs,
    private val permissionsManager: PermissionsManager
) : ViewModel() {

    private val chatsEvent = EventEmitter<ChatsEvent>()
    fun chatsEvent(): EventSource<ChatsEvent> = chatsEvent

    fun getConversations() {
        viewModelScope.launch(Dispatchers.IO) {
            val event = when {
                !permissionsManager.isDefaultSms() -> ChatsEvent.RequestDefaultSms
                !permissionsManager.hasReadSms() || !permissionsManager.hasContacts() -> ChatsEvent.RequestPermission
                else -> {
                    val lastSync = lastSyncSharedPrefs.get()
                    if (lastSync == 0L) {
                        syncDataUseCase()
                    }
                    ChatsEvent.ConversationsData(getConversationsUseCase())
                }
            }

            withContext(Dispatchers.Main) {
                chatsEvent.emit(event)
            }
        }
    }

}