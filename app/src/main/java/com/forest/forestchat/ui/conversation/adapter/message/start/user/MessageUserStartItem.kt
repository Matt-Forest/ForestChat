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
package com.forest.forestchat.ui.conversation.adapter.message.start.user

import com.forest.forestchat.ui.base.recycler.BaseItem
import com.forest.forestchat.ui.conversation.adapter.ConversationPayload
import com.forest.forestchat.ui.conversation.adapter.ConversationViewTypes
import com.forest.forestchat.ui.conversation.adapter.message.StatusUserMessage

class MessageUserStartItem(
    val messageId: Long,
    val message: String,
    val hours: String,
    val sim: Int?,
    val date: String?,
    val status: StatusUserMessage?
) : BaseItem() {

    override fun getViewType(): Int = ConversationViewTypes.MESSAGE_USER_START

    override fun isItemTheSame(oldItem: BaseItem): Boolean =
        oldItem is MessageUserStartItem && oldItem.messageId == messageId

    override fun getChangePayload(oldItem: BaseItem): Any? {
        oldItem as MessageUserStartItem

        return when {
            oldItem.status != status -> ConversationPayload.Status(status)
            else -> null
        }
    }

}