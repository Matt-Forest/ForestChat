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
package com.forest.forestchat.ui.conversation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forest.forestchat.domain.useCases.GetMessageByIdUseCase
import com.forest.forestchat.domain.useCases.GetMessagesByConversationUseCase
import com.forest.forestchat.domain.useCases.SaveMmsPartUseCase
import com.forest.forestchat.extensions.getNavigationInput
import com.forest.forestchat.manager.PermissionsManager
import com.forest.forestchat.manager.SubscriptionManagerCompat
import com.forest.forestchat.ui.conversation.adapter.MessageItemEvent
import com.zhuinden.eventemitter.EventEmitter
import com.zhuinden.eventemitter.EventSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val getMessagesByConversationUseCase: GetMessagesByConversationUseCase,
    private val getMessageByIdUseCase: GetMessageByIdUseCase,
    private val subscriptionManagerCompat: SubscriptionManagerCompat,
    private val saveMmsPartUseCase: SaveMmsPartUseCase,
    private val permissionsManager: PermissionsManager,
    handle: SavedStateHandle
) : ViewModel() {

    private val eventEmitter = EventEmitter<ConversationEvent>()
    fun eventSource(): EventSource<ConversationEvent> = eventEmitter

    private val conversation = handle.getNavigationInput<ConversationInput>().conversation

    init {
        eventEmitter.emit(ConversationEvent.BaseData(conversation.getTitle()))
        eventEmitter.emit(ConversationEvent.Loading)

        viewModelScope.launch(Dispatchers.IO) {
            val messages = getMessagesByConversationUseCase(conversation.id)

            withContext(Dispatchers.Main) {
                when (messages == null) {
                    true -> eventEmitter.emit(ConversationEvent.Empty)
                    false -> eventEmitter.emit(
                        ConversationEvent.Data(
                            messages,
                            conversation.recipients,
                            subscriptionManagerCompat.activeSubscriptionInfoList
                        )
                    )
                }
            }
        }
    }

    fun onEvent(event: MessageItemEvent) {
        when (event) {
            is MessageItemEvent.AttachmentSelected -> attachmentSelected(
                event.messageId,
                event.mmsPartId
            )
        }
    }

    private fun attachmentSelected(messageId: Long, partId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            getMessageByIdUseCase(messageId)?.mms?.parts?.firstOrNull { it.id == partId }
                ?.let { mmsPart ->
                    if (!mmsPart.isMedia()) {
                        withContext(Dispatchers.Main) {
                            when (permissionsManager.hasStorage()) {
                                true -> saveMmsPartUseCase(mmsPart)?.let {
                                    eventEmitter.emit(
                                        ConversationEvent.ViewFile(it)
                                    )
                                }
                                false -> eventEmitter.emit(ConversationEvent.RequestStoragePermission)
                            }
                        }
                    }
                }
        }
    }

}