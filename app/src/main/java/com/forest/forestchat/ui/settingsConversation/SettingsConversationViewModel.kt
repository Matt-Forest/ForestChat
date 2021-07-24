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
package com.forest.forestchat.ui.settingsConversation

import androidx.lifecycle.*
import com.forest.forestchat.domain.useCases.DeleteConversationsByThreadIdUseCase
import com.forest.forestchat.domain.useCases.GetMessagesByConversationUseCase
import com.forest.forestchat.domain.useCases.UpdateConversationUseCase
import com.forest.forestchat.extensions.getNavigationInput
import com.forest.forestchat.ui.common.media.Media
import com.forest.forestchat.ui.settingsConversation.models.SettingsConversationData
import com.forest.forestchat.ui.settingsConversation.models.SettingsConversationEvent
import com.forest.forestchat.ui.settingsConversation.models.SettingsConversationInput
import com.forest.forestchat.utils.CopyIntoClipboard
import com.zhuinden.eventemitter.EventEmitter
import com.zhuinden.eventemitter.EventSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsConversationViewModel @Inject constructor(
    private val getMessagesByConversationUseCase: GetMessagesByConversationUseCase,
    private val updateConversationUseCase: UpdateConversationUseCase,
    private val deleteConversationsByThreadIdUseCase: DeleteConversationsByThreadIdUseCase,
    private val copyIntoClipboard: CopyIntoClipboard,
    handle: SavedStateHandle
) : ViewModel() {

    private val data = MutableLiveData<SettingsConversationData>()
    fun data(): LiveData<SettingsConversationData> = data

    private val mediasData = MutableLiveData<List<Media>>()
    fun mediasData(): LiveData<List<Media>> = mediasData

    private val isArchive = MutableLiveData<Boolean>()
    fun isArchive(): LiveData<Boolean> = isArchive

    private val isBlock = MutableLiveData<Boolean>()
    fun isBlock(): LiveData<Boolean> = isBlock

    private val eventEmitter = EventEmitter<SettingsConversationEvent>()
    fun eventSource(): EventSource<SettingsConversationEvent> = eventEmitter

    private var conversation = handle.getNavigationInput<SettingsConversationInput>().conversation

    init {
        data.value = when (conversation.grouped) {
            true -> SettingsConversationData.Group(
                conversation.getTitle(),
                conversation.recipients
            )
            false -> SettingsConversationData.Single(
                conversation.getTitle(),
                conversation.recipients[0].contact != null,
                conversation.recipients
            )
        }
        isArchive.value = conversation.archived
        isBlock.value = conversation.blocked
        getMediaToUi()
    }

    private fun getMediaToUi() {
        viewModelScope.launch(Dispatchers.IO) {
            val medias = getAllMediasFromConversation()
            if (medias.isNotEmpty()) {
                mediasData.postValue(medias)
            }
        }
    }

    private suspend fun getAllMediasFromConversation(): List<Media> {
        val medias = mutableListOf<Media>()
        getMessagesByConversationUseCase(conversation.id)
            ?.forEach { message ->
                message.mms?.getPartsMedia()?.map { part ->
                    Media(
                        part.id,
                        part.getUri(),
                        part.isVideo(),
                        part.isGif()
                    )
                }?.let {
                    medias.addAll(it)
                }
            }
        return medias
    }

    fun onMediaSelected(mediaId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            mediasData.value?.let { medias ->
                val mediaSelected = medias.first { it.mediaId == mediaId }
                withContext(Dispatchers.Main) {
                    eventEmitter.emit(SettingsConversationEvent.ShowGallery(mediaSelected, medias))
                }
            }
        }
    }

    fun onProfileSelected() {
        onAddContact()
    }

    fun onProfileLongClick() {
        copyIntoClipboard.copy(conversation.recipients[0].address)
    }

    fun onTitleChange() {
        eventEmitter.emit(SettingsConversationEvent.ShowTitleUpdate(conversation.name ?: ""))
    }

    fun onTitleUpdated(newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            conversation = conversation.copy(name = newName)
            updateConversationUseCase(conversation)

            val newData = when (val actualData = data.value) {
                is SettingsConversationData.Single -> actualData.copy(name = newName)
                is SettingsConversationData.Group -> actualData.copy(name = newName)
                else -> null
            }
            data.postValue(newData)
        }
    }

    fun onArchive() {
        viewModelScope.launch(Dispatchers.IO) {
            conversation = conversation.copy(archived = conversation.archived.not())
            updateConversationUseCase(conversation)

            isArchive.postValue(conversation.archived)
        }
    }

    fun onBlock() {
        viewModelScope.launch(Dispatchers.IO) {
            conversation = conversation.copy(blocked = conversation.blocked.not())
            updateConversationUseCase(conversation)

            isBlock.postValue(conversation.blocked)
        }
    }

    fun onNotifications() {
        eventEmitter.emit(SettingsConversationEvent.ShowNotification(conversation))
    }

    fun onDeleteConversation() {
        viewModelScope.launch(Dispatchers.IO) {
            deleteConversationsByThreadIdUseCase(conversation.id)
        }
    }

    fun onAddContact() {
        val recipient = conversation.recipients[0]
        eventEmitter.emit(
            SettingsConversationEvent.ShowContact(
                recipient.contact?.lookupKey,
                recipient.address
            )
        )
    }

}