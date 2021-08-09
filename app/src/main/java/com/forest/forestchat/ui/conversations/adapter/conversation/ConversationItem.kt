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
package com.forest.forestchat.ui.conversations.adapter.conversation

import com.forest.forestchat.ui.base.recycler.BaseItem
import com.forest.forestchat.ui.common.avatar.AvatarType
import com.forest.forestchat.ui.conversations.adapter.HomeConversationViewTypes
import com.forest.forestchat.ui.conversations.adapter.HomeConversationsPayload

class ConversationItem(
    val id: Long,
    val title: String,
    val lastMessage: String,
    val date: String,
    val avatarType: AvatarType,
    val pinned: Boolean,
    val unread: Boolean,
    val draft: Boolean
) : BaseItem() {

    override fun getViewType(): Int = HomeConversationViewTypes.CONVERSATION

    override fun isItemTheSame(oldItem: BaseItem): Boolean =
        oldItem is ConversationItem && oldItem.id == id

    override fun getChangePayload(oldItem: BaseItem): Any? {
        oldItem as ConversationItem

        return when {
            oldItem.title != title -> HomeConversationsPayload.Title(title)
            oldItem.pinned != pinned -> HomeConversationsPayload.Pin(pinned)
            oldItem.unread != unread && oldItem.lastMessage != lastMessage -> HomeConversationsPayload.UpdateMessageAndMarkAsRead(
                unread,
                lastMessage
            )
            oldItem.unread != unread -> HomeConversationsPayload.MarkAsRead(unread)
            oldItem.lastMessage != lastMessage -> HomeConversationsPayload.NewLastMessage(
                lastMessage
            )
            else -> null
        }
    }

}