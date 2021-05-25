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
import com.forest.forestchat.domain.useCases.SearchContactsUseCase
import com.forest.forestchat.domain.useCases.SearchConversationsUseCase
import com.forest.forestchat.domain.useCases.synchronize.SyncDataUseCase
import com.forest.forestchat.localStorage.sharedPrefs.LastSyncSharedPrefs
import com.forest.forestchat.receiver.DefaultSmsChangedReceiver
import com.forest.forestchat.ui.home.HomeEvent
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
    private val searchConversationsUseCase: SearchConversationsUseCase,
    private val searchContactsUseCase: SearchContactsUseCase,
    private val lastSyncSharedPrefs: LastSyncSharedPrefs,
    private val permissionsManager: PermissionsManager
) : ViewModel() {

    private val chatsEvent = EventEmitter<HomeEvent>()
    fun chatsEvent(): EventSource<HomeEvent> = chatsEvent

    fun getConversations() {
        viewModelScope.launch(Dispatchers.IO) {
            val event = when {
                !permissionsManager.isDefaultSms() -> HomeEvent.RequestDefaultSms
                !permissionsManager.hasReadSms() || !permissionsManager.hasContacts() -> HomeEvent.RequestPermission
                else -> {
                    val lastSync = lastSyncSharedPrefs.get()
                    if (lastSync == 0L) {
                        withContext(Dispatchers.Main) {
                            chatsEvent.emit(HomeEvent.ChatsLoading)
                        }
                        syncDataUseCase()
                    }
                    val conversations = getConversationsUseCase()
                    when (conversations == null || conversations.isEmpty()) {
                        true -> HomeEvent.NoConversations
                        false -> HomeEvent.ConversationsData(conversations)
                    }
                }
            }

            withContext(Dispatchers.Main) {
                chatsEvent.emit(event)
            }
        }
    }

    fun onDefaultSmsChange(event: DefaultSmsChangedReceiver.ReceiverEvent) = when (event) {
        DefaultSmsChangedReceiver.ReceiverEvent.Load -> chatsEvent.emit(HomeEvent.ChatsLoading)
        DefaultSmsChangedReceiver.ReceiverEvent.Complete -> getConversations()
    }

    fun onSearchChange(search: String) {
        if (permissionsManager.isDefaultSms()
            && permissionsManager.hasReadSms()
            && permissionsManager.hasContacts()
            && search.isNotBlank()
        ) {
            viewModelScope.launch(Dispatchers.IO) {
                val conversations = searchConversationsUseCase(search)
                val contacts = searchContactsUseCase(search)

                withContext(Dispatchers.Main) {
                    chatsEvent.emit(when (conversations?.isNullOrEmpty() == true && contacts?.isNullOrEmpty() == true) {
                        true -> HomeEvent.NoSearchData
                        false -> HomeEvent.Search(conversations!!, contacts!!)
                    })
                }
            }
        } else {
            getConversations()
        }
    }

}