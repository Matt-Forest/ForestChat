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
package com.forest.forestchat.ui.conversation.adapter.messageUserContact

import com.forest.forestchat.ui.base.recycler.BaseAdapterItem
import com.forest.forestchat.ui.conversation.adapter.ConversationViewTypes

class MessageUserContactItem(
    val messageId: Long,
    val partId: Long,
    val contactName: String,
    val hours: String,
    val sim: Int?,
    val date: String?
) : BaseAdapterItem() {

    override fun getViewType(): Int = ConversationViewTypes.MESSAGE_USER_CONTACT

    override fun isItemTheSame(oldItem: BaseAdapterItem): Boolean =
        oldItem is MessageUserContactItem && oldItem.messageId == messageId

}