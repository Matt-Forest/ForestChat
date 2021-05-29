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
import com.forest.forestchat.app.PermissionsManager
import com.forest.forestchat.app.TransversalBusEvent
import com.forest.forestchat.domain.models.Conversation
import com.forest.forestchat.domain.useCases.*
import com.forest.forestchat.domain.useCases.synchronize.SyncContactsUseCase
import com.forest.forestchat.domain.useCases.synchronize.SyncConversationsUseCase
import com.forest.forestchat.domain.useCases.synchronize.SyncDataUseCase
import com.forest.forestchat.localStorage.sharedPrefs.LastSyncSharedPrefs
import com.forest.forestchat.ui.conversations.dialog.ConversationOptionType
import com.zhuinden.eventemitter.EventEmitter
import com.zhuinden.eventemitter.EventSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ConversationsViewModel @Inject constructor(
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

    private val chatsEvent = EventEmitter<ConversationEvent>()
    fun chatsEvent(): EventSource<ConversationEvent> = chatsEvent

    private var conversationSelected: Conversation? = null

    fun getConversations() {
        viewModelScope.launch(Dispatchers.IO) {
            val event = when {
                !permissionsManager.isDefaultSms() -> ConversationEvent.RequestDefaultSms
                !permissionsManager.hasReadSms() || !permissionsManager.hasContacts() -> ConversationEvent.RequestPermission
                else -> {
                    val lastSync = lastSyncSharedPrefs.get()
                    if (lastSync == 0L) {
                        withContext(Dispatchers.Main) {
                            chatsEvent.emit(ConversationEvent.Loading)
                        }
                        syncDataUseCase()
                    }
                    val conversations = getConversationsUseCase()
                    when (conversations == null || conversations.isEmpty()) {
                        true -> ConversationEvent.NoConversationsData
                        false -> ConversationEvent.ConversationsData(conversations)
                    }
                }
            }

            withContext(Dispatchers.Main) {
                chatsEvent.emit(event)
            }
        }
    }

    fun onDefaultSmsChange(event: TransversalBusEvent.DefaultSmsChangedEvent) = when (event) {
        TransversalBusEvent.DefaultSmsChangedEvent.Load -> chatsEvent.emit(ConversationEvent.Loading)
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
                    chatsEvent.emit(
                        when (conversations?.isNullOrEmpty() == true && contacts?.isNullOrEmpty() == true) {
                            true -> ConversationEvent.NoSearchData
                            false -> ConversationEvent.SearchData(
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

    fun onConversationSelected(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            getConversationUseCase(id)?.let { conversation ->
                conversationSelected = conversation
                withContext(Dispatchers.Main) {
                    chatsEvent.emit(
                        ConversationEvent.ShowConversationOptions(
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
    }

    fun conversationOptionSelected(type: ConversationOptionType) {
        conversationSelected?.let { conversation ->
            viewModelScope.launch(Dispatchers.IO) {
                when (type) {
                    ConversationOptionType.AddToContacts -> {
                        withContext(Dispatchers.Main) {
                            chatsEvent.emit(ConversationEvent.AddContact(conversation.recipients.first().address))
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
                        markAsReadUseCase(conversation)
                        getConversations()
                    }
                    ConversationOptionType.Remove -> {
                        withContext(Dispatchers.Main) {
                            chatsEvent.emit(ConversationEvent.RequestDeleteDialog(conversation.id))
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

}