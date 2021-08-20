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
package com.forest.forestchat.ui.create.group

import androidx.lifecycle.*
import com.forest.forestchat.domain.models.Recipient
import com.forest.forestchat.domain.useCases.GetOrCreateConversationByAddressesUseCase
import com.forest.forestchat.domain.useCases.UpdateConversationUseCase
import com.forest.forestchat.extensions.getNavigationInput
import com.forest.forestchat.manager.PermissionsManager
import com.forest.forestchat.ui.create.group.models.CreateGroupInput
import com.forest.forestchat.ui.create.group.models.GroupConversationEvent
import com.forest.forestchat.ui.create.group.models.GroupConversationType
import com.zhuinden.eventemitter.EventEmitter
import com.zhuinden.eventemitter.EventSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CreateGroupViewModel @Inject constructor(
    private val getOrCreateConversationByAddressesUseCase: GetOrCreateConversationByAddressesUseCase,
    private val updateConversationUseCase: UpdateConversationUseCase,
    private val permissionsManager: PermissionsManager,
    handle: SavedStateHandle
) : ViewModel() {

    private val conversationType = MutableLiveData(GroupConversationType.Group)
    fun conversationType(): LiveData<GroupConversationType> = conversationType

    private val eventEmitter = EventEmitter<GroupConversationEvent>()
    fun eventSource(): EventSource<GroupConversationEvent> = eventEmitter

    private val input = handle.getNavigationInput<CreateGroupInput>()
    private var name: String? = null

    fun updateName(name: String) {
        this.name = name
    }

    fun updateConversationType(type: GroupConversationType) {
        conversationType.value = type
    }

    fun create() {
        viewModelScope.launch(Dispatchers.IO) {
            when {
                !permissionsManager.isDefaultSms() -> {
                    withContext(Dispatchers.Main) {
                        eventEmitter.emit(GroupConversationEvent.RequestDefaultSms)
                    }
                }
                !permissionsManager.hasReadSms() || !permissionsManager.hasContacts() -> {
                    withContext(Dispatchers.Main) {
                        eventEmitter.emit(GroupConversationEvent.RequestPermission)
                    }
                }
                else -> {
                    getOrCreateConversationByAddressesUseCase(buildRecipients().map { it.address })?.let { conversation ->
                        val conv = conversation.copy(
                            name = name,
                            grouped = conversationType.value == GroupConversationType.Group
                        )
                        updateConversationUseCase(conv)
                        withContext(Dispatchers.Main) {
                            eventEmitter.emit(GroupConversationEvent.GoToConversation(conv))
                        }
                    }
                }
            }
        }
    }

    private fun buildRecipients(): List<Recipient> {
        val recipients = mutableListOf<Recipient>()
        recipients.addAll(
            input.contacts?.map { contact ->
                Recipient(
                    id = 0L,
                    address = contact.getDefaultNumber()?.address ?: contact.numbers[0].address,
                    contact = contact
                )
            } ?: listOf()
        )
        recipients.addAll(
            input.newRecipient?.map { newRec ->
                Recipient(
                    id = 0L,
                    address = newRec,
                    contact = null
                )
            } ?: listOf()
        )

        return recipients
    }

}