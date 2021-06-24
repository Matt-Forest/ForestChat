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

import android.view.ViewGroup
import com.forest.forestchat.domain.models.message.Message
import com.forest.forestchat.ui.base.recycler.BaseAdapter
import com.forest.forestchat.ui.base.recycler.BaseHolder
import com.forest.forestchat.ui.conversation.adapter.messageRecipientEnd.MessageRecipientEndHolder
import com.forest.forestchat.ui.conversation.adapter.messageRecipientMiddle.MessageRecipientMiddleHolder
import com.forest.forestchat.ui.conversation.adapter.messageRecipientSingle.MessageRecipientSingleHolder
import com.forest.forestchat.ui.conversation.adapter.messageRecipientStart.MessageRecipientStartHolder
import com.forest.forestchat.ui.conversation.adapter.messageUserEnd.MessageUserEndHolder
import com.forest.forestchat.ui.conversation.adapter.messageUserMiddle.MessageUserMiddleHolder
import com.forest.forestchat.ui.conversation.adapter.messageUserSingle.MessageUserSingleHolder
import com.forest.forestchat.ui.conversation.adapter.messageUserStart.MessageUserStartHolder

class ConversationAdapter : BaseAdapter() {

    override fun buildViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<*>? =
        when (viewType) {
            ConversationViewTypes.MESSAGE_USER_SINGLE -> MessageUserSingleHolder(parent)
            ConversationViewTypes.MESSAGE_USER_START -> MessageUserStartHolder(parent)
            ConversationViewTypes.MESSAGE_USER_END -> MessageUserEndHolder(parent)
            ConversationViewTypes.MESSAGE_USER_MIDDLE -> MessageUserMiddleHolder(parent)
            ConversationViewTypes.MESSAGE_RECIPIENT_START -> MessageRecipientStartHolder(parent)
            ConversationViewTypes.MESSAGE_RECIPIENT_SINGLE -> MessageRecipientSingleHolder(parent)
            ConversationViewTypes.MESSAGE_RECIPIENT_END -> MessageRecipientEndHolder(parent)
            ConversationViewTypes.MESSAGE_RECIPIENT_MIDDLE -> MessageRecipientMiddleHolder(parent)
            else -> null
        }

    fun setMessages(messages: List<Message>) {

    }

}