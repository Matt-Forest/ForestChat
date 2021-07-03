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
package com.forest.forestchat.ui.conversation.adapter.messageMedias.recipient

import android.view.ViewGroup
import androidx.core.view.isGone
import com.forest.forestchat.R
import com.forest.forestchat.databinding.HolderMessageRecipientMediaBinding
import com.forest.forestchat.extensions.invisibleIf
import com.forest.forestchat.extensions.visibleIf
import com.forest.forestchat.ui.conversation.adapter.messageMedias.MessageMediasBaseHolder

class MessageRecipientMediasHolder(
    parent: ViewGroup
) : MessageMediasBaseHolder<MessageRecipientMediasItem>(
    parent,
    R.layout.holder_message_recipient_media
) {

    private val binding = HolderMessageRecipientMediaBinding.bind(itemView)

    override fun bind(item: MessageRecipientMediasItem) {
        with(binding) {
            date.text = item.date
            date.visibleIf { item.date != null }
            info.text = item.hours
            name.text = item.name
            name.visibleIf { item.name != null }
            item.avatarType?.let { avatar.setAvatar(it) }
            avatar.invisibleIf { item.avatarType == null }

            setMedias(medias, item.medias)

            itemView.setOnClickListener {
                info.visibleIf { info.isGone }
            }
        }
    }

}