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

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.telephony.SubscriptionInfo
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.getSystemService
import androidx.core.view.inputmethod.InputContentInfoCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.forest.forestchat.R
import com.forest.forestchat.databinding.NavigationConversationBinding
import com.forest.forestchat.extensions.*
import com.forest.forestchat.ui.common.ForestEditText
import com.forest.forestchat.ui.conversation.adapter.ConversationAdapter
import com.forest.forestchat.ui.conversation.adapter.MessageItemEvent
import com.forest.forestchat.ui.conversation.attachmentsAdapter.AttachmentsAdapter
import com.forest.forestchat.ui.conversation.dialog.MessageOptionType
import com.forest.forestchat.ui.conversation.dialog.MessageOptionsDialog
import com.forest.forestchat.ui.conversation.models.Attachment
import com.forest.forestchat.ui.conversation.models.AttachmentSelection
import com.forest.forestchat.ui.conversation.models.ConversationEvent
import com.forest.forestchat.ui.conversation.models.ConversationState
import com.forest.forestchat.ui.gallery.GalleryInput

class ConversationNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding: NavigationConversationBinding

    lateinit var onMessageEvent: (MessageItemEvent) -> Unit
    lateinit var optionSelected: (MessageOptionType) -> Unit
    lateinit var onTextToSendChange: (String) -> Unit
    lateinit var sendOrAddAttachment: () -> Unit
    lateinit var toggleAddAttachment: () -> Unit
    lateinit var toggleSimCard: () -> Unit
    lateinit var onAttachmentSelected: (AttachmentSelection) -> Unit
    lateinit var onInputContentSelected: (InputContentInfoCompat) -> Unit
    lateinit var onCallClick: () -> Unit
    lateinit var onRemoveAttachment: (Int) -> Unit

    private var conversationAdapter = ConversationAdapter(context) { onMessageEvent(it) }
    private var attachmentsAdapter = AttachmentsAdapter { onRemoveAttachment(it) }

    init {
        val layoutInflater = LayoutInflater.from(context)
        binding = NavigationConversationBinding.inflate(layoutInflater, this)

        with(binding) {
            back.setOnClickListener { findNavController().popBackStack() }
            simCard.setOnClickListener { toggleSimCard() }
            phone.setOnClickListener { onCallClick() }
            messageToSend.doAfterTextChanged { text ->
                onTextToSendChange(text?.toString() ?: "")
            }
            messageToSend.setListener(object : ForestEditText.Listener {
                override fun onInputContentSelected(inputContentInfo: InputContentInfoCompat) {
                    this@ConversationNavigationView.onInputContentSelected(inputContentInfo)
                }
            })
            sendOrAttachment.setOnClickListener {
                context.closeKeyboard(binding.messageToSend)
                sendOrAddAttachment()
            }
            addAttachment.setOnClickListener { toggleAddAttachment() }
            gallery.setOnClickListener { onAttachmentSelected(AttachmentSelection.Gallery) }
            camera.setOnClickListener { onAttachmentSelected(AttachmentSelection.Camera) }
            contact.setOnClickListener { onAttachmentSelected(AttachmentSelection.Contact) }
            attachmentRecycler.layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        }
    }

    fun event(event: ConversationEvent) {
        when (event) {
            is ConversationEvent.ShowMessageOptions -> {
                MessageOptionsDialog(context, event.canCopy) { optionSelected(it) }
                    .create()
                    .show()
            }
            is ConversationEvent.ShowMessageDetails -> {
                AlertDialog.Builder(context)
                    .setTitle(R.string.message_details_title)
                    .setMessage(event.details)
                    .setCancelable(true)
                    .show()
            }
            is ConversationEvent.ShowGallery -> {
                val input = GalleryInput(event.medias, event.mediaSelected)
                findNavController().navigate(ConversationFragmentDirections.goToGallery(input))
            }
            else -> null
        }
    }

    fun setLoading(isVisible: Boolean) {
        binding.loading.visibleIf { isVisible }
    }

    fun updateState(state: ConversationState) {
        with(binding) {
            when (state) {
                is ConversationState.Empty -> {
                    emptyAvatars.updateAvatars(state.avatarType)
                    emptyLabel.text = state.title
                    emptyPhone.text = state.phone
                    recyclerConversation.gone()
                    emptyContainer.visible()
                }
                is ConversationState.Data -> {
                    if (recyclerConversation.adapter !== conversationAdapter) {
                        recyclerConversation.adapter = conversationAdapter
                    }
                    conversationAdapter.apply {
                        setMessages(state.messages, state.recipients, state.subscriptionsInfo)
                    }
                    emptyContainer.gone()
                    recyclerConversation.visible()
                }
            }
        }
    }

    fun updateTitle(title: String) {
        binding.conversationTitle.text = title
    }

    fun updateMessageToSend(message: String) {
        if (message != binding.messageToSend.text?.toString()) {
            binding.messageToSend.setText(message)
        }
    }

    fun updateAttachmentVisibility(isVisible: Boolean) {
        with(binding) {
            attachmentButtonContainer.visibleIf { isVisible }
            if (!addAttachment.isVisible) {
                sendOrAttachment.animate().rotation(
                    when (isVisible) {
                        true -> 45F
                        false -> 0F
                    }
                ).start()
            }
        }
    }

    fun activateSending(activate: Boolean) {
        binding.sendOrAttachment.setImageDrawable(
            when (activate) {
                true -> R.drawable.ic_send
                false -> R.drawable.ic_add
            }.asDrawable(context)
        )
        binding.addAttachment.visibleIf { activate }
    }

    fun updateSimInformation(simInfo: SubscriptionInfo?) {
        val isNotInit = binding.simCard.isVisible
        binding.simCard.visibleIf { simInfo != null }
        if (simInfo != null) {
            binding.simImage.contentDescription = R.string.conversation_sim_content_describe
                .format(context, simInfo.displayName)
            binding.simIndex.text = simInfo.simSlotIndex.plus(1).toString()

            if (isNotInit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.getSystemService<Vibrator>()
                        ?.vibrate(
                            VibrationEffect.createOneShot(
                                40,
                                VibrationEffect.DEFAULT_AMPLITUDE
                            )
                        )
                }
                context.makeToast(
                    context.getString(
                        R.string.conversation_sim_toast,
                        simInfo.simSlotIndex + 1, simInfo.displayName
                    )
                )
            }
        }
    }

    fun updateAttachments(attachments: List<Attachment>) {
        with(binding.attachmentRecycler) {
            if (adapter !== attachmentsAdapter) {
                adapter = attachmentsAdapter
            }
            attachmentsAdapter.apply {
                setAttachments(attachments)
            }

            visibleIf { attachments.isNotEmpty() }
        }
    }

}