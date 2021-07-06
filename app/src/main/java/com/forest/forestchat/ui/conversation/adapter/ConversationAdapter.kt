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
import android.telephony.SubscriptionInfo
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.ViewGroup
import com.forest.forestchat.domain.models.Recipient
import com.forest.forestchat.domain.models.message.Message
import com.forest.forestchat.domain.models.message.MessageType
import com.forest.forestchat.extensions.getMessageDate
import com.forest.forestchat.extensions.getMessageHours
import com.forest.forestchat.extensions.isSameDayWithOther
import com.forest.forestchat.ui.base.recycler.BaseAdapter
import com.forest.forestchat.ui.base.recycler.BaseItem
import com.forest.forestchat.ui.base.recycler.BaseHolder
import com.forest.forestchat.ui.common.avatar.AvatarType
import com.forest.forestchat.ui.common.mappers.buildSingleAvatar
import com.forest.forestchat.ui.common.media.Media
import com.forest.forestchat.ui.conversation.adapter.message.contact.recipient.MessageRecipientContactHolder
import com.forest.forestchat.ui.conversation.adapter.message.contact.recipient.MessageRecipientContactItem
import com.forest.forestchat.ui.conversation.adapter.message.contact.user.MessageUserContactHolder
import com.forest.forestchat.ui.conversation.adapter.message.contact.user.MessageUserContactItem
import com.forest.forestchat.ui.conversation.adapter.message.end.recipient.MessageRecipientEndHolder
import com.forest.forestchat.ui.conversation.adapter.message.end.recipient.MessageRecipientEndItem
import com.forest.forestchat.ui.conversation.adapter.message.end.user.MessageUserEndHolder
import com.forest.forestchat.ui.conversation.adapter.message.end.user.MessageUserEndItem
import com.forest.forestchat.ui.conversation.adapter.message.file.recipient.MessageRecipientFileHolder
import com.forest.forestchat.ui.conversation.adapter.message.file.recipient.MessageRecipientFileItem
import com.forest.forestchat.ui.conversation.adapter.message.file.user.MessageUserFileHolder
import com.forest.forestchat.ui.conversation.adapter.message.file.user.MessageUserFileItem
import com.forest.forestchat.ui.conversation.adapter.message.medias.recipient.MessageRecipientMediasHolder
import com.forest.forestchat.ui.conversation.adapter.message.medias.recipient.MessageRecipientMediasItem
import com.forest.forestchat.ui.conversation.adapter.message.medias.user.MessageUserMediasHolder
import com.forest.forestchat.ui.conversation.adapter.message.medias.user.MessageUserMediasItem
import com.forest.forestchat.ui.conversation.adapter.message.middle.recipient.MessageRecipientMiddleHolder
import com.forest.forestchat.ui.conversation.adapter.message.middle.recipient.MessageRecipientMiddleItem
import com.forest.forestchat.ui.conversation.adapter.message.middle.user.MessageUserMiddleHolder
import com.forest.forestchat.ui.conversation.adapter.message.middle.user.MessageUserMiddleItem
import com.forest.forestchat.ui.conversation.adapter.message.single.recipient.MessageRecipientSingleHolder
import com.forest.forestchat.ui.conversation.adapter.message.single.recipient.MessageRecipientSingleItem
import com.forest.forestchat.ui.conversation.adapter.message.single.user.MessageUserSingleHolder
import com.forest.forestchat.ui.conversation.adapter.message.single.user.MessageUserSingleItem
import com.forest.forestchat.ui.conversation.adapter.message.start.recipient.MessageRecipientStartHolder
import com.forest.forestchat.ui.conversation.adapter.message.start.recipient.MessageRecipientStartItem
import com.forest.forestchat.ui.conversation.adapter.message.start.user.MessageUserStartHolder
import com.forest.forestchat.ui.conversation.adapter.message.start.user.MessageUserStartItem
import ezvcard.Ezvcard

class ConversationAdapter(
    val context: Context,
    private val onEvent: (MessageItemEvent) -> Unit
) : BaseAdapter() {

    override fun buildViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<*>? =
        when (viewType) {
            ConversationViewTypes.MESSAGE_USER_SINGLE -> MessageUserSingleHolder(parent, onEvent)
            ConversationViewTypes.MESSAGE_USER_START -> MessageUserStartHolder(parent, onEvent)
            ConversationViewTypes.MESSAGE_USER_END -> MessageUserEndHolder(parent, onEvent)
            ConversationViewTypes.MESSAGE_USER_MIDDLE -> MessageUserMiddleHolder(parent, onEvent)
            ConversationViewTypes.MESSAGE_USER_CONTACT -> MessageUserContactHolder(parent, onEvent)
            ConversationViewTypes.MESSAGE_USER_FILE -> MessageUserFileHolder(parent, onEvent)
            ConversationViewTypes.MESSAGE_USER_MEDIA -> MessageUserMediasHolder(parent, onEvent)
            ConversationViewTypes.MESSAGE_RECIPIENT_START -> MessageRecipientStartHolder(parent, onEvent)
            ConversationViewTypes.MESSAGE_RECIPIENT_SINGLE -> MessageRecipientSingleHolder(parent, onEvent)
            ConversationViewTypes.MESSAGE_RECIPIENT_END -> MessageRecipientEndHolder(parent, onEvent)
            ConversationViewTypes.MESSAGE_RECIPIENT_MIDDLE -> MessageRecipientMiddleHolder(parent, onEvent)
            ConversationViewTypes.MESSAGE_RECIPIENT_CONTACT -> MessageRecipientContactHolder(parent, onEvent)
            ConversationViewTypes.MESSAGE_RECIPIENT_FILE -> MessageRecipientFileHolder(parent, onEvent)
            ConversationViewTypes.MESSAGE_RECIPIENT_MEDIA -> MessageRecipientMediasHolder(parent, onEvent)
            else -> null
        }

    fun setMessages(
        messages: List<Message>,
        recipients: List<Recipient>,
        subsInfo: List<SubscriptionInfo>
    ) {
        val items = mutableListOf<BaseItem>()

        messages.forEachIndexed { index, message ->
            // Show the date only if the next message has an another date or if he is the last message
            val showDate = when (messages.size > index + 1) {
                true -> !message.date.isSameDayWithOther(messages[index + 1].date)
                false -> true
            }

            // Because the recycler layout is reversed, the previous message is at the bottom and
            // the next message is on the top
            val previousIsSameSenderAndDate = when (messages.size > index + 1) {
                true -> {
                    val previous = messages[index + 1]
                    message.compareSender(previous) && message.date.isSameDayWithOther(previous.date)
                }
                false -> false
            }
            val nextIsSameSenderAndDate = when (index - 1 >= 0) {
                true -> {
                    val next = messages[index - 1]
                    message.compareSender(next) && message.date.isSameDayWithOther(next.date)
                }
                false -> false
            }

            val recipient = getRecipientFromAddress(recipients, message.address)

            when (message.type) {
                MessageType.Sms -> messageSms(
                    message,
                    previousIsSameSenderAndDate,
                    nextIsSameSenderAndDate,
                    recipient,
                    showDate,
                    subsInfo
                ).let { item ->
                    items.add(item)
                }
                MessageType.Mms -> {
                    messageMms(
                        message,
                        recipient,
                        showDate,
                        previousIsSameSenderAndDate,
                        subsInfo
                    ).let { items.addAll(it) }
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
        recipient: Recipient?,
        showDate: Boolean,
        subs: List<SubscriptionInfo>
    ): BaseItem {
        val messageDate = when (showDate) {
            true -> message.date.getMessageDate(context)
            false -> null
        }

        return when {
            previousIsSameSender && nextIsSameSender ->
                when (message.isUser()) {
                    true -> MessageUserMiddleItem(
                        messageId = message.id,
                        message = message.getText(),
                        hours = message.date.getMessageHours(context),
                        sim = message.subId?.let { subId -> getSimSlot(subs, subId) },
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
                        sim = message.subId?.let { subId -> getSimSlot(subs, subId) },
                        date = messageDate
                    )
                    false -> MessageRecipientStartItem(
                        messageId = message.id,
                        message = message.getText(),
                        hours = message.date.getMessageHours(context),
                        name = buildRecipientName(recipient, previousIsSameSender),
                        avatarType = buildAvatar(recipient, previousIsSameSender),
                        date = messageDate
                    )
                }
            previousIsSameSender && !nextIsSameSender ->
                when (message.isUser()) {
                    true -> MessageUserEndItem(
                        messageId = message.id,
                        message = message.getText(),
                        hours = message.date.getMessageHours(context),
                        sim = message.subId?.let { subId -> getSimSlot(subs, subId) },
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
                        sim = message.subId?.let { subId -> getSimSlot(subs, subId) },
                        date = messageDate
                    )
                    false -> MessageRecipientSingleItem(
                        messageId = message.id,
                        message = message.getText(),
                        hours = message.date.getMessageHours(context),
                        name = buildRecipientName(recipient, previousIsSameSender),
                        avatarType = buildAvatar(recipient, previousIsSameSender),
                        date = messageDate
                    )
                }
        }
    }

    private fun messageMms(
        message: Message,
        recipient: Recipient?,
        showDate: Boolean,
        nextIsSameSenderAndDate: Boolean,
        subs: List<SubscriptionInfo>
    ): List<BaseItem> {
        val items = mutableListOf<BaseItem>()

        // If there are Text in parts
        mmsPartText(
            message,
            recipient,
            showDate,
            nextIsSameSenderAndDate,
            subs
        )?.let { items.add(it) }

        // If there are Media (Image and video) in parts
        mmsPartMedia(message, recipient, showDate, nextIsSameSenderAndDate, subs)?.let {
            items.add(
                it
            )
        }

        // If there are Contact card in parts
        mmsPartContactCard(
            message,
            recipient,
            showDate,
            nextIsSameSenderAndDate,
            subs
        )?.let { items.addAll(it) }

        // If there are Files in parts
        mmsPartFile(
            message,
            recipient,
            showDate,
            nextIsSameSenderAndDate,
            subs
        )?.let { items.addAll(it) }

        return items
    }

    private fun mmsPartText(
        message: Message,
        recipient: Recipient?,
        showDate: Boolean,
        nextIsSameSender: Boolean,
        subs: List<SubscriptionInfo>
    ): BaseItem? = message.mms?.getPartsText()?.ifEmpty { null }?.let { textParts ->
        val messageDate = when (showDate) {
            true -> message.date.getMessageDate(context)
            false -> null
        }
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
                sim = message.subId?.let { subId -> getSimSlot(subs, subId) },
                date = messageDate
            )
            false -> MessageRecipientSingleItem(
                messageId = message.id,
                message = text,
                hours = message.date.getMessageHours(context),
                name = buildRecipientName(recipient, nextIsSameSender),
                avatarType = buildAvatar(recipient, nextIsSameSender),
                date = messageDate
            )
        }
    }

    private fun mmsPartMedia(
        message: Message,
        recipient: Recipient?,
        showDate: Boolean,
        nextIsSameSender: Boolean,
        subs: List<SubscriptionInfo>
    ): BaseItem? {
        val messageDate = when (showDate) {
            true -> message.date.getMessageDate(context)
            false -> null
        }

        return message.mms?.getPartsMedia()?.ifEmpty { null }?.let { mediaParts ->
            when (message.isUser()) {
                true -> MessageUserMediasItem(
                    messageId = message.id,
                    medias = mediaParts.map { part ->
                        Media(
                            part.id,
                            part.getUri(),
                            part.isVideo(),
                            part.isGif()
                        )
                    },
                    hours = message.date.getMessageHours(context),
                    sim = message.subId?.let { subId -> getSimSlot(subs, subId) },
                    date = messageDate
                )
                false -> MessageRecipientMediasItem(
                    messageId = message.id,
                    medias = mediaParts.map { part ->
                        Media(
                            part.id,
                            part.getUri(),
                            part.isVideo(),
                            part.isGif()
                        )
                    },
                    hours = message.date.getMessageHours(context),
                    name = buildRecipientName(recipient, nextIsSameSender),
                    avatarType = buildAvatar(recipient, nextIsSameSender),
                    date = messageDate
                )
            }
        }
    }

    private fun mmsPartContactCard(
        message: Message,
        recipient: Recipient?,
        showDate: Boolean,
        nextIsSameSender: Boolean,
        subs: List<SubscriptionInfo>
    ): List<BaseItem>? =
        message.mms?.getPartsContactCard()?.ifEmpty { null }?.let { contactCardParts ->
            val items = mutableListOf<BaseItem>()

            contactCardParts.forEachIndexed { index, part ->
                context.contentResolver.openInputStream(part.getUri())?.use {
                    val card = Ezvcard.parse(it).first()
                    val messageDate = when (showDate && index == contactCardParts.size - 1) {
                        true -> message.date.getMessageDate(context)
                        false -> null
                    }

                    items.add(
                        when (message.isUser()) {
                            true -> MessageUserContactItem(
                                messageId = message.id,
                                partId = part.id,
                                contactName = card.formattedName.value,
                                hours = message.date.getMessageHours(context),
                                sim = message.subId?.let { subId -> getSimSlot(subs, subId) },
                                date = messageDate
                            )
                            false -> MessageRecipientContactItem(
                                messageId = message.id,
                                partId = part.id,
                                contactName = card.formattedName.value,
                                hours = message.date.getMessageHours(context),
                                name = buildRecipientName(recipient, nextIsSameSender),
                                avatarType = buildAvatar(recipient, nextIsSameSender),
                                date = messageDate
                            )
                        }
                    )
                }
            }

            items
        }

    private fun mmsPartFile(
        message: Message,
        recipient: Recipient?,
        showDate: Boolean,
        nextIsSameSender: Boolean,
        subs: List<SubscriptionInfo>
    ): List<BaseItem>? = message.mms?.getPartsOther()?.ifEmpty { null }?.let { fileParts ->
        val items = mutableListOf<BaseItem>()

        fileParts.forEachIndexed { index, part ->
            context.contentResolver.openInputStream(part.getUri())?.use {
                val messageDate = when (showDate && index == fileParts.size - 1) {
                    true -> message.date.getMessageDate(context)
                    false -> null
                }
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
                            partId = part.id,
                            name = part.name ?: "",
                            size = size,
                            hours = message.date.getMessageHours(context),
                            sim = message.subId?.let { subId -> getSimSlot(subs, subId) },
                            date = messageDate
                        )
                        false -> MessageRecipientFileItem(
                            messageId = message.id,
                            partId = part.id,
                            fileName = part.name ?: "",
                            size = size,
                            hours = message.date.getMessageHours(context),
                            name = buildRecipientName(recipient, nextIsSameSender),
                            avatarType = buildAvatar(recipient, nextIsSameSender),
                            date = messageDate
                        )
                    }
                )
            }
        }

        items
    }

    private fun getRecipientFromAddress(recipients: List<Recipient>, address: String?): Recipient? =
        recipients.firstOrNull { PhoneNumberUtils.compare(it.address, address) }

    private fun buildRecipientName(recipient: Recipient?, nextIsSameSender: Boolean): String? =
        when (nextIsSameSender) {
            true -> null
            false -> recipient?.getDisplayName()
        }

    private fun buildAvatar(recipient: Recipient?, nextIsSameSender: Boolean): AvatarType.Single? =
        when (nextIsSameSender) {
            true -> null
            false -> buildSingleAvatar(recipient?.contact, false)
        }

    private fun getSimSlot(subs: List<SubscriptionInfo>, subId: Int): Int? {
        val subscription = subs.find { sub -> sub.subscriptionId == subId }
        return subscription?.simSlotIndex?.plus(1)
    }

}