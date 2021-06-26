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
package com.forest.forestchat.ui.conversation.adapter

import android.content.Context
import android.graphics.Typeface
import android.telephony.PhoneNumberUtils
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.ViewGroup
import com.forest.forestchat.domain.models.Recipient
import com.forest.forestchat.domain.models.message.Message
import com.forest.forestchat.domain.models.message.MessageType
import com.forest.forestchat.extensions.getMessageDate
import com.forest.forestchat.extensions.getMessageHours
import com.forest.forestchat.ui.base.recycler.BaseAdapter
import com.forest.forestchat.ui.base.recycler.BaseAdapterItem
import com.forest.forestchat.ui.base.recycler.BaseHolder
import com.forest.forestchat.ui.common.mappers.buildSingleAvatar
import com.forest.forestchat.ui.common.media.Media
import com.forest.forestchat.ui.conversation.adapter.messageRecipientContact.MessageRecipientContactHolder
import com.forest.forestchat.ui.conversation.adapter.messageRecipientContact.MessageRecipientContactItem
import com.forest.forestchat.ui.conversation.adapter.messageRecipientEnd.MessageRecipientEndHolder
import com.forest.forestchat.ui.conversation.adapter.messageRecipientEnd.MessageRecipientEndItem
import com.forest.forestchat.ui.conversation.adapter.messageRecipientFile.MessageRecipientFileHolder
import com.forest.forestchat.ui.conversation.adapter.messageRecipientFile.MessageRecipientFileItem
import com.forest.forestchat.ui.conversation.adapter.messageRecipientMedias.MessageRecipientMediasHolder
import com.forest.forestchat.ui.conversation.adapter.messageRecipientMedias.MessageRecipientMediasItem
import com.forest.forestchat.ui.conversation.adapter.messageRecipientMiddle.MessageRecipientMiddleHolder
import com.forest.forestchat.ui.conversation.adapter.messageRecipientMiddle.MessageRecipientMiddleItem
import com.forest.forestchat.ui.conversation.adapter.messageRecipientSingle.MessageRecipientSingleHolder
import com.forest.forestchat.ui.conversation.adapter.messageRecipientSingle.MessageRecipientSingleItem
import com.forest.forestchat.ui.conversation.adapter.messageRecipientStart.MessageRecipientStartHolder
import com.forest.forestchat.ui.conversation.adapter.messageRecipientStart.MessageRecipientStartItem
import com.forest.forestchat.ui.conversation.adapter.messageUserContact.MessageUserContactHolder
import com.forest.forestchat.ui.conversation.adapter.messageUserContact.MessageUserContactItem
import com.forest.forestchat.ui.conversation.adapter.messageUserEnd.MessageUserEndHolder
import com.forest.forestchat.ui.conversation.adapter.messageUserEnd.MessageUserEndItem
import com.forest.forestchat.ui.conversation.adapter.messageUserFile.MessageUserFileHolder
import com.forest.forestchat.ui.conversation.adapter.messageUserFile.MessageUserFileItem
import com.forest.forestchat.ui.conversation.adapter.messageUserMedias.MessageUserMediasHolder
import com.forest.forestchat.ui.conversation.adapter.messageUserMedias.MessageUserMediasItem
import com.forest.forestchat.ui.conversation.adapter.messageUserMiddle.MessageUserMiddleHolder
import com.forest.forestchat.ui.conversation.adapter.messageUserMiddle.MessageUserMiddleItem
import com.forest.forestchat.ui.conversation.adapter.messageUserSingle.MessageUserSingleHolder
import com.forest.forestchat.ui.conversation.adapter.messageUserSingle.MessageUserSingleItem
import com.forest.forestchat.ui.conversation.adapter.messageUserStart.MessageUserStartHolder
import com.forest.forestchat.ui.conversation.adapter.messageUserStart.MessageUserStartItem
import ezvcard.Ezvcard

class ConversationAdapter(
    val context: Context
) : BaseAdapter() {

    override fun buildViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<*>? =
        when (viewType) {
            ConversationViewTypes.MESSAGE_USER_SINGLE -> MessageUserSingleHolder(parent)
            ConversationViewTypes.MESSAGE_USER_START -> MessageUserStartHolder(parent)
            ConversationViewTypes.MESSAGE_USER_END -> MessageUserEndHolder(parent)
            ConversationViewTypes.MESSAGE_USER_MIDDLE -> MessageUserMiddleHolder(parent)
            ConversationViewTypes.MESSAGE_USER_CONTACT -> MessageUserContactHolder(parent)
            ConversationViewTypes.MESSAGE_USER_FILE -> MessageUserFileHolder(parent)
            ConversationViewTypes.MESSAGE_USER_MEDIA -> MessageUserMediasHolder(parent)
            ConversationViewTypes.MESSAGE_RECIPIENT_START -> MessageRecipientStartHolder(parent)
            ConversationViewTypes.MESSAGE_RECIPIENT_SINGLE -> MessageRecipientSingleHolder(parent)
            ConversationViewTypes.MESSAGE_RECIPIENT_END -> MessageRecipientEndHolder(parent)
            ConversationViewTypes.MESSAGE_RECIPIENT_MIDDLE -> MessageRecipientMiddleHolder(parent)
            ConversationViewTypes.MESSAGE_RECIPIENT_CONTACT -> MessageRecipientContactHolder(parent)
            ConversationViewTypes.MESSAGE_RECIPIENT_FILE -> MessageRecipientFileHolder(parent)
            ConversationViewTypes.MESSAGE_RECIPIENT_MEDIA -> MessageRecipientMediasHolder(parent)
            else -> null
        }

    fun setMessages(messages: List<Message>, recipients: List<Recipient>) {
        val items = mutableListOf<BaseAdapterItem>()

        messages.forEachIndexed { index, message ->
            val previousIsSameSender = when (index - 1 >= 0) {
                true -> message.compareSender(messages[index - 1])
                false -> false
            }
            val nextIsSameSender = when (messages.size > index + 1) {
                true -> message.compareSender(messages[index + 1])
                false -> false
            }

            val recipient = getRecipientFromAddress(recipients, message.address)

            when (message.type) {
                MessageType.Sms -> messageSms(
                    message,
                    previousIsSameSender,
                    nextIsSameSender,
                    recipient
                ).let { item ->
                    items.add(item)
                }
                MessageType.Mms -> {
                    messageMms(message, recipient).let { items.addAll(it) }
                }
                MessageType.Unknown -> null
            }
        }

        submitList(items)
    }

    private fun messageSms(
        message: Message,
        previousIsSameSender: Boolean,
        nextIsSameSender: Boolean,
        recipient: Recipient?
    ): BaseAdapterItem = when {
        previousIsSameSender && nextIsSameSender ->
            when (message.isUser()) {
                true -> MessageUserMiddleItem(
                    messageId = message.id,
                    message = message.getText(),
                    hours = message.date.getMessageHours(context)
                )
                false -> MessageRecipientMiddleItem(
                    messageId = message.id,
                    message = message.getText(),
                    hours = message.date.getMessageHours(context)
                )
            }
        !previousIsSameSender && nextIsSameSender ->
            when (message.isUser()) {
                true -> MessageUserStartItem(
                    messageId = message.id,
                    message = message.getText(),
                    hours = message.date.getMessageHours(context),
                    date = message.date.getMessageDate(context)
                )
                false -> MessageRecipientStartItem(
                    messageId = message.id,
                    message = message.getText(),
                    hours = message.date.getMessageHours(context),
                    name = recipient?.getDisplayName() ?: "",
                    avatarType = buildSingleAvatar(recipient?.contact, false),
                    date = message.date.getMessageDate(context)
                )
            }
        previousIsSameSender && !nextIsSameSender ->
            when (message.isUser()) {
                true -> MessageUserEndItem(
                    messageId = message.id,
                    message = message.getText(),
                    hours = message.date.getMessageHours(context)
                )
                false -> MessageRecipientEndItem(
                    messageId = message.id,
                    message = message.getText(),
                    hours = message.date.getMessageHours(context)
                )
            }
        else ->
            when (message.isUser()) {
                true -> MessageUserSingleItem(
                    messageId = message.id,
                    message = message.getText(),
                    hours = message.date.getMessageHours(context),
                    date = message.date.getMessageDate(context)
                )
                false -> MessageRecipientSingleItem(
                    messageId = message.id,
                    message = message.getText(),
                    hours = message.date.getMessageHours(context),
                    name = recipient?.getDisplayName() ?: "",
                    avatarType = buildSingleAvatar(recipient?.contact, false),
                    date = message.date.getMessageDate(context)
                )
            }
    }

    private fun messageMms(
        message: Message,
        recipient: Recipient?
    ): List<BaseAdapterItem> {
        val items = mutableListOf<BaseAdapterItem>()

        // If there are Text in parts
        mmsPartText(message, recipient)?.let { items.add(it) }

        // If there are Media (Image and video) in parts
        mmsPartMedia(message, recipient)?.let { items.add(it) }

        // If there are Contact card in parts
        mmsPartContactCard(message, recipient)?.let { items.addAll(it) }

        // If there are Files in parts
        mmsPartFile(message, recipient)?.let { items.addAll(it) }

        return items
    }

    private fun mmsPartText(
        message: Message,
        recipient: Recipient?
    ): BaseAdapterItem? = message.mms?.getPartsText()?.let { textParts ->
        val subject = message.mms.getCleansedSubject()
        val body = textParts
            .mapNotNull { part -> part.text }
            .filter { text -> text.isNotBlank() }
            .joinToString("\n")

        val text = when {
            subject.isNotBlank() -> {
                val spannable =
                    SpannableString(if (body.isNotBlank()) "$subject\n$body" else subject)
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD), 0, subject.length,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                )
                spannable
            }
            else -> body
        }.toString()

        when (message.isUser()) {
            true -> MessageUserSingleItem(
                messageId = message.id,
                message = text,
                hours = message.date.getMessageHours(context),
                date = message.date.getMessageDate(context)
            )
            false -> MessageRecipientSingleItem(
                messageId = message.id,
                message = text,
                hours = message.date.getMessageHours(context),
                name = recipient?.getDisplayName() ?: "",
                avatarType = buildSingleAvatar(recipient?.contact, false),
                date = message.date.getMessageDate(context)
            )
        }
    }

    private fun mmsPartMedia(
        message: Message,
        recipient: Recipient?
    ): BaseAdapterItem? = message.mms?.getPartsMedia()?.let { mediaParts ->
        when (message.isUser()) {
            true -> MessageUserMediasItem(
                messageId = message.id,
                medias = mediaParts.map { part -> Media(part.getUri(), part.isVideo()) },
                hours = message.date.getMessageHours(context),
                date = message.date.getMessageDate(context)
            )
            false -> MessageRecipientMediasItem(
                messageId = message.id,
                medias = mediaParts.map { part -> Media(part.getUri(), part.isVideo()) },
                hours = message.date.getMessageHours(context),
                name = recipient?.getDisplayName() ?: "",
                avatarType = buildSingleAvatar(recipient?.contact, false),
                date = message.date.getMessageDate(context)
            )
        }
    }

    private fun mmsPartContactCard(
        message: Message,
        recipient: Recipient?
    ): List<BaseAdapterItem>? = message.mms?.getPartsContactCard()?.let { contactCardParts ->
        val items = mutableListOf<BaseAdapterItem>()

        contactCardParts.forEach { part ->
            context.contentResolver.openInputStream(part.getUri())?.use {
                val card = Ezvcard.parse(it).first()

                items.add(
                    when (message.isUser()) {
                        true -> MessageUserContactItem(
                            messageId = message.id,
                            contactName = card.formattedName.value,
                            hours = message.date.getMessageHours(context),
                            date = message.date.getMessageDate(context)
                        )
                        false -> MessageRecipientContactItem(
                            messageId = message.id,
                            contactName = card.formattedName.value,
                            hours = message.date.getMessageHours(context),
                            name = recipient?.getDisplayName() ?: "",
                            avatarType = buildSingleAvatar(recipient?.contact, false),
                            date = message.date.getMessageDate(context)
                        )
                    }
                )
            }
        }

        items
    }

    private fun mmsPartFile(
        message: Message,
        recipient: Recipient?
    ): List<BaseAdapterItem>? = message.mms?.getPartsOther()?.let { fileParts ->
        val items = mutableListOf<BaseAdapterItem>()

        fileParts.forEach { part ->
            context.contentResolver.openInputStream(part.getUri())?.use {
                val size = when (val bytes = it.available()) {
                    in 0..999 -> "$bytes B"
                    in 1000..999999 -> "${"%.1f".format(bytes / 1000f)} KB"
                    in 1000000..9999999 -> "${"%.1f".format(bytes / 1000000f)} MB"
                    else -> "${"%.1f".format(bytes / 1000000000f)} GB"
                }

                items.add(
                    when (message.isUser()) {
                        true -> MessageUserFileItem(
                            messageId = message.id,
                            name = part.name ?: "",
                            size = size,
                            hours = message.date.getMessageHours(context),
                            date = message.date.getMessageDate(context)
                        )
                        false -> MessageRecipientFileItem(
                            messageId = message.id,
                            fileName = part.name ?: "",
                            size = size,
                            hours = message.date.getMessageHours(context),
                            name = recipient?.getDisplayName() ?: "",
                            avatarType = buildSingleAvatar(recipient?.contact, false),
                            date = message.date.getMessageDate(context)
                        )
                    }
                )
            }
        }

        items
    }

    private fun getRecipientFromAddress(recipients: List<Recipient>, address: String?): Recipient? =
        recipients.firstOrNull { PhoneNumberUtils.compare(it.address, address) }

}