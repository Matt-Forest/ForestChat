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

import android.net.Uri
import android.telephony.SubscriptionInfo
import androidx.core.view.inputmethod.InputContentInfoCompat
import androidx.lifecycle.*
import com.forest.forestchat.domain.models.message.Message
import com.forest.forestchat.domain.models.message.MessageType
import com.forest.forestchat.domain.useCases.*
import com.forest.forestchat.extensions.getNavigationInput
import com.forest.forestchat.manager.*
import com.forest.forestchat.ui.common.mappers.buildAvatar
import com.forest.forestchat.ui.common.media.Media
import com.forest.forestchat.ui.conversation.adapter.MessageItemEvent
import com.forest.forestchat.ui.conversation.dialog.MessageOptionType
import com.forest.forestchat.ui.conversation.models.*
import com.forest.forestchat.utils.CopyIntoClipboard
import com.forest.forestchat.utils.MessageDetailsFormatter
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
    private val deleteMessageUseCase: DeleteMessageUseCase,
    private val updateLastMessageConversationUseCase: UpdateLastMessageConversationUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val resendMessageUseCase: ResendMessageUseCase,
    private val markAsReadUseCase: MarkAsReadUseCase,
    private val permissionsManager: PermissionsManager,
    private val copyIntoClipboard: CopyIntoClipboard,
    private val messageDetailsFormatter: MessageDetailsFormatter,
    private val activeThreadManager: ActiveThreadManager,
    private val notificationManager: NotificationManager,
    private val forestChatShortCutManager: ForestChatShortCutManager,
    handle: SavedStateHandle
) : ViewModel() {

    private val eventEmitter = EventEmitter<ConversationEvent>()
    fun eventSource(): EventSource<ConversationEvent> = eventEmitter

    private val isLoading = MutableLiveData<Boolean>()
    fun isLoading(): LiveData<Boolean> = isLoading

    private val title = MutableLiveData<String>()
    fun title(): LiveData<String> = title

    private val messageToSend = MutableLiveData<String>()
    fun messageToSend(): LiveData<String> = messageToSend

    private val attachmentVisibility = MutableLiveData(false)
    fun attachmentVisibility(): LiveData<Boolean> = attachmentVisibility

    private val activateSending = MutableLiveData(false)
    fun activateSending(): LiveData<Boolean> = activateSending

    private val attachments = MutableLiveData<MutableList<Attachment>>()
    fun attachments(): LiveData<MutableList<Attachment>> = attachments

    private val state = MutableLiveData<ConversationState>()
    fun state(): LiveData<ConversationState> = state

    private val simInfo = MutableLiveData<SubscriptionInfo?>()
    fun simInfo(): LiveData<SubscriptionInfo?> = simInfo

    private val conversation = handle.getNavigationInput<ConversationInput>().conversation
    private var messageSelected: Message? = null

    init {
        title.value = conversation.getTitle()
        isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            markAsReadUseCase(conversation.id)
            notificationManager.update(conversation.id)
            forestChatShortCutManager.updateBadge()
        }
        updateMessages()
        initSimInformation()
    }

    fun updateMessages() {
        viewModelScope.launch(Dispatchers.IO) {
            val messages = getMessagesByConversationUseCase(conversation.id)

            when (messages == null) {
                true -> {
                    val phone = when (conversation.recipients.size == 1) {
                        true -> conversation.recipients[0].getNumberPhone()
                        false -> null
                    }
                    state.postValue(
                        ConversationState.Empty(
                            buildAvatar(conversation.recipients),
                            conversation.getTitle(),
                            phone
                        )
                    )
                }
                false -> state.postValue(
                    ConversationState.Data(
                        messages,
                        conversation.recipients,
                        subscriptionManagerCompat.activeSubscriptionInfoList
                    )
                )
            }
            isLoading.postValue(false)
        }
    }

    private fun initSimInformation() {
        viewModelScope.launch(Dispatchers.IO) {
            val messages = getMessagesByConversationUseCase(conversation.id)
            val latestSubId = messages?.lastOrNull()?.subId ?: -1
            val subs = subscriptionManagerCompat.activeSubscriptionInfoList
            val sub = if (subs.size > 1) subs.firstOrNull { it.subscriptionId == latestSubId }
                ?: subs[0] else null

            simInfo.postValue(sub)
        }
    }

    fun onEvent(event: MessageItemEvent) {
        when (event) {
            is MessageItemEvent.AttachmentSelected -> attachmentSelected(
                event.messageId,
                event.mmsPartId
            )
            is MessageItemEvent.MessageSelected -> messageSelected(event.messageId)
            is MessageItemEvent.MediaSelected -> prepareGallery(event.partId)
        }
    }

    /**
     * get all medias from the conversation
     */
    private fun prepareGallery(partId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
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

            val mediaSelected = medias.first { it.mediaId == partId }
            withContext(Dispatchers.Main) {
                eventEmitter.emit(ConversationEvent.ShowGallery(mediaSelected, medias))
            }
        }
    }

    fun onMessageOptionSelected(optionSelected: MessageOptionType) {
        messageSelected?.let { message ->
            when (optionSelected) {
                MessageOptionType.Copy -> copyIntoClipboard.copyMessage(message)
                MessageOptionType.Remove -> {
                    viewModelScope.launch(Dispatchers.IO) {
                        deleteMessageUseCase(message.id)
                        updateLastMessageConversationUseCase(conversation)
                        updateMessages()
                    }
                }
                MessageOptionType.Details -> eventEmitter.emit(
                    ConversationEvent.ShowMessageDetails(
                        messageDetailsFormatter(message)
                    )
                )
                MessageOptionType.Resend -> {
                    viewModelScope.launch(Dispatchers.IO) {
                        resendMessageUseCase(message.id)
                    }
                }
            }
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
                                        ConversationEvent.ShowFile(it)
                                    )
                                }
                                false -> eventEmitter.emit(ConversationEvent.RequestStoragePermission)
                            }
                        }
                    }
                }
        }
    }

    private fun messageSelected(messageId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            getMessageByIdUseCase(messageId)?.let { message ->
                messageSelected = message
                withContext(Dispatchers.Main) {
                    eventEmitter.emit(
                        ConversationEvent.ShowMessageOptions(
                            message.type == MessageType.Sms,
                            message.isFailed()
                        )
                    )
                }
            }
        }
    }

    fun onTextToSendChange(newText: String) {
        if (messageToSend.value != newText) {
            messageToSend.value = newText
            checkSendingState()
        }
    }

    private fun checkSendingState() {
        val canSendingElement =
            !messageToSend.value.isNullOrEmpty() || !attachments.value.isNullOrEmpty()
        activateSending.value = canSendingElement
    }

    fun sendOrAddAttachment() {
        when (activateSending.value) {
            true -> {
                when {
                    !permissionsManager.isDefaultSms() -> eventEmitter.emit(ConversationEvent.RequestDefaultSms)
                    !permissionsManager.hasSendSms() -> eventEmitter.emit(ConversationEvent.RequestSmsPermission)
                    else -> {
                        sendNewMessage()
                        attachments.value = mutableListOf()
                        attachmentVisibility.value = false
                        activateSending.value = false
                        onTextToSendChange("")
                    }
                }
            }
            false -> toggleAddAttachment()
        }
    }

    private fun sendNewMessage() {
        val subId = simInfo.value?.subscriptionId ?: -1
        val body = messageToSend.value ?: ""
        val attachments = attachments.value ?: listOf()

        viewModelScope.launch(Dispatchers.IO) {
            sendMessageUseCase(subId, conversation, body, attachments)
        }
    }

    fun attachmentSelected(attachmentSelection: AttachmentSelection) {
        attachmentVisibility.value = false
        when (attachmentSelection) {
            AttachmentSelection.Camera -> {
                if (permissionsManager.hasStorage()) {
                    eventEmitter.emit(ConversationEvent.RequestCamera)
                } else {
                    eventEmitter.emit(ConversationEvent.RequestStoragePermission)
                }
            }
            AttachmentSelection.Gallery -> eventEmitter.emit(ConversationEvent.RequestGallery)
            AttachmentSelection.Contact -> eventEmitter.emit(ConversationEvent.RequestContact)
        }
    }

    fun toggleAddAttachment() {
        attachmentVisibility.value?.let { actual ->
            attachmentVisibility.value = !actual
        }
    }

    fun toggleSim() {
        val subs = subscriptionManagerCompat.activeSubscriptionInfoList
        val subIndex = subs.indexOfFirst { it.subscriptionId == simInfo.value?.subscriptionId }
        val subscription = when {
            subIndex == -1 -> null
            subIndex < subs.size - 1 -> subs[subIndex + 1]
            else -> subs[0]
        }

        simInfo.value = subscription
    }

    fun addImageAttachment(uris: List<Uri>) {
        addAttachments(uris.map { Attachment.Image(uri = it) })
    }

    fun addContactAttachment(vCard: String) {
        addAttachments(listOf(Attachment.Contact(vCard)))
    }

    fun inputContentSelected(inputContentInfoCompat: InputContentInfoCompat) {
        addAttachments(listOf(Attachment.Image(inputContent = inputContentInfoCompat)))
    }

    private fun addAttachments(attach: List<Attachment>) {
        var attachments = attachments.value
        if (attachments == null) {
            attachments = mutableListOf()
        }
        attachments.addAll(attach)
        this.attachments.value = attachments
        checkSendingState()
    }

    fun removeAttachment(index: Int) {
        val attachments = attachments.value
        attachments?.removeAt(index)
        this.attachments.value = attachments

        checkSendingState()
    }

    fun makeACall() {
        conversation.recipients.firstOrNull()?.address?.let { address ->
            eventEmitter.emit(ConversationEvent.Call(address, permissionsManager.hasCalling()))
        }
    }

    fun updateActiveConversation(isActive: Boolean) {
        activeThreadManager.setActiveThread(
            when (isActive) {
                true -> conversation.id
                false -> null
            }
        )
    }

    fun settings() {
        eventEmitter.emit(ConversationEvent.GoToSettings(conversation))
    }

}