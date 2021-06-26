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
package com.forest.forestchat.ui.conversation.adapter.messageRecipientContact

import android.view.ViewGroup
import com.forest.forestchat.R
import com.forest.forestchat.databinding.HolderMessageRecipientContactBinding
import com.forest.forestchat.extensions.visible
import com.forest.forestchat.extensions.visibleIf
import com.forest.forestchat.ui.base.recycler.BaseHolder
import com.forest.forestchat.ui.common.avatar.AvatarType

class MessageRecipientContactHolder(
    parent: ViewGroup
) : BaseHolder<MessageRecipientContactItem>(parent, R.layout.holder_message_recipient_contact) {

    private val binding = HolderMessageRecipientContactBinding.bind(itemView)

    override fun bind(item: MessageRecipientContactItem) {
        with(binding) {
            date.text = item.date
            date.visibleIf { item.date != null }
            info.text = item.hours
            contactAvatar.setAvatar(AvatarType.Single.Profile)
            contactName.text = item.contactName
            name.text = item.name
            avatar.setAvatar(item.avatarType)

            itemView.setOnClickListener {
                info.visible()
            }
        }
    }

}