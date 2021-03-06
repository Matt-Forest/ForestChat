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
package com.forest.forestchat.ui.conversation.adapter.message.end.recipient

import android.view.ViewGroup
import androidx.core.view.isGone
import com.forest.forestchat.R
import com.forest.forestchat.databinding.HolderMessageRecipientEndBinding
import com.forest.forestchat.extensions.visibleIf
import com.forest.forestchat.ui.base.recycler.BaseHolder
import com.forest.forestchat.ui.conversation.adapter.MessageItemEvent

class MessageRecipientEndHolder(
    parent: ViewGroup,
    private val onEvent: (MessageItemEvent) -> Unit
) : BaseHolder<MessageRecipientEndItem>(parent, R.layout.holder_message_recipient_end) {

    private val binding = HolderMessageRecipientEndBinding.bind(itemView)

    override fun bind(item: MessageRecipientEndItem) {
        with(binding) {
            message.text = item.message
            info.text = item.hours

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