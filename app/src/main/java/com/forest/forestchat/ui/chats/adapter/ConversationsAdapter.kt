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
package com.forest.forestchat.ui.chats.adapter

import android.content.Context
import android.view.ViewGroup
import com.forest.forestchat.R
import com.forest.forestchat.domain.models.Conversation
import com.forest.forestchat.domain.models.Recipient
import com.forest.forestchat.extensions.getConversationTimestamp
import com.forest.forestchat.ui.base.recycler.BaseAdapter
import com.forest.forestchat.ui.base.recycler.BaseHolder
import com.forest.forestchat.ui.common.avatar.AvatarType

class ConversationsAdapter : BaseAdapter() {

    override fun buildViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<*>? =
        ConversationHolder(parent)

    fun setConversations(context: Context, conversations: List<Conversation>) {
        val items = conversations.map { conversation ->
            ConversationItem(
                id = conversation.id,
                title = conversation.getTitle(),
                lastMessage = when {
                    conversation.draft?.isNotEmpty() == true -> conversation.draft
                    conversation.lastMessage?.isUser() == true -> context.getString(
                        R.string.chats_sender_user,
                        conversation.lastMessage.getSummary()
                    )
                    else -> conversation.lastMessage?.getSummary()
                } ?: "",
                date = conversation.lastMessage?.date?.takeIf { it > 0 }
                    ?.getConversationTimestamp(context) ?: "",
                avatarType = buildAvatar(conversation.recipients),
                pinned = conversation.pinned,
                unread = conversation.lastMessage?.read == false
            )
        }
        submitList(items)
    }

    private fun buildAvatar(recipients: List<Recipient>): AvatarType = when {
        recipients.size == 1 -> buildSingleAvatar(recipients[0], false)
        recipients.size > 1 -> AvatarType.Group(
            buildSingleAvatar(recipients[0], true),
            buildSingleAvatar(recipients[1], true)
        )
        else -> AvatarType.Single.Profile
    }

    private fun buildSingleAvatar(recipient: Recipient, isFromGroup: Boolean): AvatarType.Single =
        when {
            recipient.contact?.photoUri?.isNotBlank() == true ->
                AvatarType.Single.Image(recipient.contact.photoUri)
            recipient.contact?.name?.isNotBlank() == true ->
                AvatarType.Single.Letters(buildInitial(recipient.contact.name, isFromGroup))
            else -> AvatarType.Single.Profile
        }

    private fun buildInitial(name: String, isFromGroup: Boolean): String {
        val initials = name.substringBefore(',')
            .split(" ")
            .filter { it.isNotEmpty() }
            .map { it[0] }
            .filter { initial -> initial.isLetterOrDigit() }
            .map { it.toString() }

        return when {
            isFromGroup -> initials.first()
            initials.size > 1 -> initials.first() + initials.last()
            else -> initials.first()
        }
    }

}