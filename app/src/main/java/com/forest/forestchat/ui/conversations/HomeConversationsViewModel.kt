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
package com.forest.forestchat.ui.conversations

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forest.forestchat.R
import com.forest.forestchat.app.TransversalBusEvent
import com.forest.forestchat.domain.models.Conversation
import com.forest.forestchat.domain.useCases.*
import com.forest.forestchat.domain.useCases.synchronize.SyncContactsUseCase
import com.forest.forestchat.domain.useCases.synchronize.SyncConversationsUseCase
import com.forest.forestchat.domain.useCases.synchronize.SyncDataUseCase
import com.forest.forestchat.localStorage.sharedPrefs.LastSyncSharedPrefs
import com.forest.forestchat.manager.PermissionsManager
import com.forest.forestchat.ui.conversations.adapter.conversation.ConversationItemEvent
import com.forest.forestchat.ui.conversations.dialog.ConversationOptionType
import com.forest.forestchat.ui.conversations.models.HomeConversationEvent
import com.forest.forestchat.ui.conversations.models.HomeConversationsState
import com.zhuinden.eventemitter.EventEmitter
import com.zhuinden.eventemitter.EventSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeConversationsViewModel @Inject constructor(
    private val getConversationsUseCase: GetConversationsUseCase,
    private val getConversationUseCase: GetConversationUseCase,
    private val updateConversationUseCase: UpdateConversationUseCase,
    private val markAsReadUseCase: MarkAsReadUseCase,
    private val deleteConversationsByThreadIdUseCase: DeleteConversationsByThreadIdUseCase,
    private val syncDataUseCase: SyncDataUseCase,
    private val searchConversationsUseCase: SearchConversationsUseCase,
    private val searchContactsUseCase: SearchContactsUseCase,
    private val syncContactsUseCase: SyncContactsUseCase,
    private val syncConversationUseCase: SyncConversationsUseCase,
    private val lastSyncSharedPrefs: LastSyncSharedPrefs,
    private val permissionsManager: PermissionsManager
) : ViewModel() {

    private val isLoading = MutableLiveData(true)
    fun isLoading(): LiveData<Boolean> = isLoading

    private val bannerVisible = MutableLiveData(false)
    fun bannerVisible(): LiveData<Boolean> = bannerVisible

    private val state = MutableLiveData<HomeConversationsState>()
    fun state(): LiveData<HomeConversationsState> = state

    private val eventEmitter = EventEmitter<HomeConversationEvent>()
    fun eventSource(): EventSource<HomeConversationEvent> = eventEmitter

    private var conversationSelected: Conversation? = null
    private var bannerIsLoad = false

    fun getConversations() {
        viewModelScope.launch(Dispatchers.IO) {
            when {
                !permissionsManager.isDefaultSms() -> {
                    withContext(Dispatchers.Main) {
                        eventEmitter.emit(HomeConversationEvent.RequestDefaultSms)
                    }
                }
                !permissionsManager.hasReadSms() || !permissionsManager.hasContacts() -> {
                    withContext(Dispatchers.Main) {
                        eventEmitter.emit(HomeConversationEvent.RequestPermission)
                        state.value = HomeConversationsState.RequestPermission
                    }
                }
                else -> {
                    val lastSync = lastSyncSharedPrefs.get()
                    if (lastSync == 0L) {
                        isLoading.postValue(true)
                        syncDataUseCase()
                    }
                    val conversations = getConversationsUseCase()
                    when (conversations.isNullOrEmpty()) {
                        true -> state.postValue(HomeConversationsState.Empty(R.string.conversations_empty_conversation))
                        false -> state.postValue(HomeConversationsState.Conversations(conversations))
                    }
                    bannerVisible.postValue(bannerIsLoad)
                    isLoading.postValue(false)
                }
            }
        }
    }

    fun onDefaultSmsChange(event: TransversalBusEvent.DefaultSmsChangedEvent) = when (event) {
        TransversalBusEvent.DefaultSmsChangedEvent.Load -> isLoading.value = true
        TransversalBusEvent.DefaultSmsChangedEvent.Complete -> getConversations()
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

                bannerVisible.postValue(false)
                when (conversations.isNullOrEmpty() && contacts.isNullOrEmpty()) {
                    true -> state.postValue(HomeConversationsState.Empty(R.string.conversations_empty_search))
                    false -> state.postValue(
                        HomeConversationsState.Search(
                            conversations!!,
                            contacts!!
                        )
                    )
                }
            }
        } else {
            getConversations()
        }
    }

    fun onConversationEvent(event: ConversationItemEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            when (event) {
                is ConversationItemEvent.Selected -> onConversationEventSelected(event.id)
                is ConversationItemEvent.Clicked -> onConversationEventClicked(event.id)
            }
        }
    }

    private suspend fun onConversationEventSelected(id: Long) {
        getConversationUseCase(id)?.let { conversation ->
            conversationSelected = conversation
            withContext(Dispatchers.Main) {
                eventEmitter.emit(
                    HomeConversationEvent.ShowConversationOptions(
                        showAddToContacts = conversation.recipients.first()
                            .takeIf { recipient -> recipient.contact == null } != null,
                        showPin = !conversation.pinned,
                        showPinnedOff = conversation.pinned,
                        showMarkAsRead = conversation.lastMessage?.read == false
                    )
                )
            }
        }
    }

    private suspend fun onConversationEventClicked(id: Long) {
        getConversationUseCase(id)?.let { conversation ->
            withContext(Dispatchers.Main) {
                eventEmitter.emit(HomeConversationEvent.GoToConversation(conversation))
            }
        }
    }

    fun conversationOptionSelected(type: ConversationOptionType) {
        conversationSelected?.let { conversation ->
            viewModelScope.launch(Dispatchers.IO) {
                when (type) {
                    ConversationOptionType.AddToContacts -> {
                        withContext(Dispatchers.Main) {
                            eventEmitter.emit(HomeConversationEvent.AddContact(conversation.recipients.first().address))
                        }
                    }
                    ConversationOptionType.Pin -> {
                        updateConversationUseCase(conversation.copy(pinned = true))
                        getConversations()
                    }
                    ConversationOptionType.PinnedOff -> {
                        updateConversationUseCase(conversation.copy(pinned = false))
                        getConversations()
                    }
                    ConversationOptionType.Archive -> {
                        updateConversationUseCase(conversation.copy(archived = true))
                        getConversations()
                    }
                    ConversationOptionType.Block -> {
                        withContext(Dispatchers.Main) {
                            updateConversationUseCase(conversation.copy(blocked = true))
                            getConversations()
                        }
                    }
                    ConversationOptionType.MarkAsRead -> {
                        markAsReadUseCase(conversation.id)
                        getConversations()
                    }
                    ConversationOptionType.Remove -> {
                        withContext(Dispatchers.Main) {
                            eventEmitter.emit(HomeConversationEvent.RequestDeleteDialog(conversation.id))
                        }
                    }
                }
            }
            conversationSelected = null
        }
    }

    fun removeConversation(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteConversationsByThreadIdUseCase(id)
            getConversations()
        }
    }

    fun onContactChanged() {
        viewModelScope.launch(Dispatchers.IO) {
            syncContactsUseCase()
            syncConversationUseCase()
            getConversations()
        }
    }

    fun bannerIsLoad(isLoad: Boolean) {
        bannerIsLoad = isLoad
        if (state.value is HomeConversationsState.Conversations) {
            bannerVisible.value = true
        }
    }

}