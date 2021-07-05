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
package com.forest.forestchat.ui.conversation.adapter.messageRecipientFile

import android.view.ViewGroup
import androidx.core.view.isGone
import com.forest.forestchat.R
import com.forest.forestchat.databinding.HolderMessageRecipientFileBinding
import com.forest.forestchat.extensions.invisibleIf
import com.forest.forestchat.extensions.visible
import com.forest.forestchat.extensions.visibleIf
import com.forest.forestchat.ui.base.recycler.BaseHolder
import com.forest.forestchat.ui.conversation.adapter.MessageItemEvent

class MessageRecipientFileHolder(
    parent: ViewGroup,
    private val onEvent: (MessageItemEvent) -> Unit
) : BaseHolder<MessageRecipientFileItem>(parent, R.layout.holder_message_recipient_file) {

    private val binding = HolderMessageRecipientFileBinding.bind(itemView)

    override fun bind(item: MessageRecipientFileItem) {
        with(binding) {
            date.text = item.date
            date.visibleIf { item.date != null }
            info.text = item.hours
            fileName.text = item.fileName
            label.text = item.size

            name.text = item.name
            name.visibleIf { item.name != null }
            item.avatarType?.let { avatar.setAvatar(it) }
            avatar.invisibleIf { item.avatarType == null }

            card.setOnClickListener {
                onEvent(
                    MessageItemEvent.AttachmentSelected(
                        item.messageId,
                        item.partId
                    )
                )
            }

            itemView.setOnClickListener {
                info.visibleIf { info.isGone }
            }
            itemView.setOnLongClickListener {
                onEvent(MessageItemEvent.MessageSelected(item.messageId))
                true
            }
        }
    }

}