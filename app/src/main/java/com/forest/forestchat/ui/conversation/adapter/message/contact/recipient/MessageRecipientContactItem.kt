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
package com.forest.forestchat.ui.conversation.adapter.message.contact.recipient

import com.forest.forestchat.ui.base.recycler.BaseItem
import com.forest.forestchat.ui.common.avatar.AvatarType
import com.forest.forestchat.ui.conversation.adapter.ConversationViewTypes

class MessageRecipientContactItem(
    val messageId: Long,
    val partId: Long,
    val contactName: String,
    val avatarType: AvatarType.Single?,
    val name: String?,
    val hours: String,
    val date: String?
) : BaseItem() {

    override fun getViewType(): Int = ConversationViewTypes.MESSAGE_RECIPIENT_CONTACT

    override fun isItemTheSame(oldItem: BaseItem): Boolean =
        oldItem is MessageRecipientContactItem && oldItem.messageId == messageId

}