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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private val eventEmitter = EventEmitter<HomeConversationEvent>()
    fun eventSource(): EventSource<HomeConversationEvent> = eventEmitter

    private var event: HomeConversationEvent? = null
    private var conversationSelected: Conversation? = null
    private var adsActivated: Boolean = false

    fun getConversations() {
        viewModelScope.launch(Dispatchers.IO) {
            val event = when {
                !permissionsManager.isDefaultSms() -> HomeConversationEvent.RequestDefaultSms
                !permissionsManager.hasReadSms() || !permissionsManager.hasContacts() -> HomeConversationEvent.RequestPermission
                else -> {
                    val lastSync = lastSyncSharedPrefs.get()
                    if (lastSync == 0L) {
                        withContext(Dispatchers.Main) {
                            updateEvent(HomeConversationEvent.Loading)
                        }
                        syncDataUseCase()
                    }
                    val conversations = getConversationsUseCase()
                    when (conversations == null || conversations.isEmpty()) {
                        true -> HomeConversationEvent.NoConversationsData
                        false -> HomeConversationEvent.ConversationsData(conversations, adsActivated)
                    }
                }
            }

            withContext(Dispatchers.Main) {
                updateEvent(event)
            }
        }
    }

    private fun updateEvent(newEvent: HomeConversationEvent) {
        event = newEvent
        eventEmitter.emit(newEvent)
    }

    fun onDefaultSmsChange(event: TransversalBusEvent.DefaultSmsChangedEvent) = when (event) {
        TransversalBusEvent.DefaultSmsChangedEvent.Load -> updateEvent(HomeConversationEvent.Loading)
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

                withContext(Dispatchers.Main) {
                    updateEvent(
                        when (conversations?.isNullOrEmpty() == true && contacts?.isNullOrEmpty() == true) {
                            true -> HomeConversationEvent.NoSearchData
                            false -> HomeConversationEvent.SearchData(
                                conversations!!,
                                contacts!!
                            )
                        }
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
                updateEvent(
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
                updateEvent(HomeConversationEvent.GoToConversation(conversation))
            }
        }
    }

    fun conversationOptionSelected(type: ConversationOptionType) {
        conversationSelected?.let { conversation ->
            viewModelScope.launch(Dispatchers.IO) {
                when (type) {
                    ConversationOptionType.AddToContacts -> {
                        withContext(Dispatchers.Main) {
                            updateEvent(HomeConversationEvent.AddContact(conversation.recipients.first().address))
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
                            updateEvent(HomeConversationEvent.RequestDeleteDialog(conversation.id))
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

    fun activateAds() {
        adsActivated = true
        event?.let {
            if (it is HomeConversationEvent.ConversationsData) {
                updateEvent(it.copy(adsActivated = true))
            }
        }
    }

}