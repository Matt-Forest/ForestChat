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
package com.forest.forestchat.ui.common.conversations.adapter

import android.content.Context
import android.view.ViewGroup
import com.forest.forestchat.R
import com.forest.forestchat.domain.models.Conversation
import com.forest.forestchat.extensions.getConversationTimestamp
import com.forest.forestchat.ui.base.recycler.BaseAdapter
import com.forest.forestchat.ui.base.recycler.BaseHolder
import com.forest.forestchat.ui.base.recycler.BaseItem
import com.forest.forestchat.ui.common.mappers.buildAvatar
import com.forest.forestchat.ui.common.conversations.adapter.conversation.ConversationHolder
import com.forest.forestchat.ui.common.conversations.adapter.conversation.ConversationItem
import com.forest.forestchat.ui.common.conversations.adapter.conversation.ConversationItemEvent
import com.forest.forestchat.ui.common.conversations.adapter.nativeAd.NativeAdHolder
import com.forest.forestchat.ui.common.conversations.adapter.nativeAd.NativeAdItem

class ConversationsAdapter(
    private val onEvent: (ConversationItemEvent) -> Unit
) : BaseAdapter() {

    override fun buildViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<*>? =
        when (viewType) {
            ConversationViewTypes.CONVERSATION -> ConversationHolder(parent, onEvent)
            ConversationViewTypes.NATIVE_AD -> NativeAdHolder(parent)
            else -> null
        }

    override fun onPayload(holder: BaseHolder<BaseItem>, position: Int, payload: Any) {
        holder as ConversationHolder
        payload as ConversationsPayload

        holder.onPayload(payload)
    }

    fun setConversations(context: Context, conversations: List<Conversation>, isArchived: Boolean) {
        val items = mutableListOf<BaseItem>()
        conversations.forEachIndexed { index, conversation ->
            if (index % 5 == 0 && !isArchived) {
                items.add(NativeAdItem())
            }
            items.add(ConversationItem(
                id = conversation.id,
                title = conversation.getTitle(),
                lastMessage = when {
                    conversation.draft?.isNotEmpty() == true -> conversation.draft
                    conversation.lastMessage?.isUser() == true -> context.getString(
                        R.string.conversations_sender_user,
                        conversation.lastMessage.getSummary()
                    )
                    else -> conversation.lastMessage?.getSummary()
                } ?: "",
                date = conversation.lastMessage?.date?.takeIf { it > 0 }
                    ?.getConversationTimestamp(context) ?: "",
                avatarType = buildAvatar(conversation.recipients),
                pinned = conversation.pinned,
                unread = conversation.lastMessage?.read == false,
                draft = false
            ))
        }

        submitList(items)
    }

}