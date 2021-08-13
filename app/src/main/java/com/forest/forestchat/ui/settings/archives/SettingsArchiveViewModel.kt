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
package com.forest.forestchat.ui.settings.archives

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forest.forestchat.domain.models.Conversation
import com.forest.forestchat.domain.useCases.DeleteConversationsByThreadIdUseCase
import com.forest.forestchat.domain.useCases.GetConversationUseCase
import com.forest.forestchat.domain.useCases.GetConversationsArchivedUseCase
import com.forest.forestchat.domain.useCases.UpdateConversationUseCase
import com.forest.forestchat.ui.common.conversations.adapter.conversation.ConversationItemEvent
import com.forest.forestchat.ui.common.conversations.dialog.ConversationOptionType
import com.forest.forestchat.ui.settings.archives.models.SettingsArchiveEvent
import com.forest.forestchat.ui.settings.archives.models.SettingsArchiveState
import com.zhuinden.eventemitter.EventEmitter
import com.zhuinden.eventemitter.EventSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsArchiveViewModel @Inject constructor(
    private val getConversationsArchivedUseCase: GetConversationsArchivedUseCase,
    private val getConversationUseCase: GetConversationUseCase,
    private val updateConversationUseCase: UpdateConversationUseCase,
    private val deleteConversationsByThreadIdUseCase: DeleteConversationsByThreadIdUseCase
) : ViewModel() {

    private val isLoading = MutableLiveData(true)
    fun isLoading(): LiveData<Boolean> = isLoading

    private val state = MutableLiveData<SettingsArchiveState>()
    fun state(): LiveData<SettingsArchiveState> = state

    private val eventEmitter = EventEmitter<SettingsArchiveEvent>()
    fun eventSource(): EventSource<SettingsArchiveEvent> = eventEmitter

    private var conversationSelected: Conversation? = null

    init {
        getConversations()
    }

    private fun getConversations() {
        isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val conversations = getConversationsArchivedUseCase()
            when (conversations.isNullOrEmpty()) {
                true -> state.postValue(SettingsArchiveState.Empty)
                false -> state.postValue(SettingsArchiveState.Conversations(conversations))
            }
            isLoading.postValue(false)
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
                eventEmitter.emit(SettingsArchiveEvent.ShowConversationOptions)
            }
        }
    }

    private suspend fun onConversationEventClicked(id: Long) {
        getConversationUseCase(id)?.let { conversation ->
            withContext(Dispatchers.Main) {
                eventEmitter.emit(SettingsArchiveEvent.GoToConversation(conversation))
            }
        }
    }

    fun conversationOptionSelected(type: ConversationOptionType) {
        conversationSelected?.let { conversation ->
            viewModelScope.launch(Dispatchers.IO) {
                when (type) {
                    ConversationOptionType.UnArchive -> {
                        updateConversationUseCase(conversation.copy(archived = false))
                        getConversations()
                    }
                    ConversationOptionType.Block -> {
                        withContext(Dispatchers.Main) {
                            updateConversationUseCase(conversation.copy(blocked = true))
                            getConversations()
                        }
                    }
                    ConversationOptionType.Remove -> {
                        withContext(Dispatchers.Main) {
                            eventEmitter.emit(SettingsArchiveEvent.RequestDeleteDialog(conversation.id))
                        }
                    }
                    else -> null
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

}