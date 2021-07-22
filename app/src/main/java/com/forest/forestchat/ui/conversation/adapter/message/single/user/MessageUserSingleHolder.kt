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
package com.forest.forestchat.ui.conversation.adapter.message.single.user

import android.view.ViewGroup
import androidx.core.view.isGone
import com.forest.forestchat.R
import com.forest.forestchat.databinding.HolderMessageUserSingleBinding
import com.forest.forestchat.extensions.asColor
import com.forest.forestchat.extensions.asString
import com.forest.forestchat.extensions.visibleIf
import com.forest.forestchat.ui.base.recycler.BaseHolder
import com.forest.forestchat.ui.conversation.adapter.ConversationPayload
import com.forest.forestchat.ui.conversation.adapter.MessageItemEvent
import com.forest.forestchat.ui.conversation.adapter.message.StatusUserMessage

class MessageUserSingleHolder(
    parent: ViewGroup,
    private val onEvent: (MessageItemEvent) -> Unit
) : BaseHolder<MessageUserSingleItem>(parent, R.layout.holder_message_user_single) {

    private val binding = HolderMessageUserSingleBinding.bind(itemView)

    override fun bind(item: MessageUserSingleItem) {
        with(binding) {
            date.text = item.date
            date.visibleIf { item.date != null }
            message.text = item.message

            info.hours.text = item.hours
            info.sim.text = item.sim.toString()
            info.sim.visibleIf { item.sim != null }
            info.simCard.visibleIf { item.sim != null }

            setupStatusMessage(item.status)

            itemView.setOnClickListener {
                if (item.status == null) {
                    info.container.visibleIf { info.container.isGone }
                }
            }
            itemView.setOnLongClickListener {
                onEvent(MessageItemEvent.MessageSelected(item.messageId))
                true
            }
        }
    }

    private fun setupStatusMessage(statusUserMessage: StatusUserMessage?) {
        with(binding.info) {
            status.text = when (statusUserMessage) {
                StatusUserMessage.Sending -> R.string.message_status_sending.asString(context)
                StatusUserMessage.Failed -> R.string.message_status_failed.asString(context)
                else -> null
            }
            status.setTextColor(
                when (statusUserMessage == StatusUserMessage.Failed) {
                    true -> R.color.error
                    false -> R.color.text_50
                }.asColor(context)
            )
            container.visibleIf { statusUserMessage != null }
        }
    }

    fun onPayload(payload: ConversationPayload) {
        when (payload) {
            is ConversationPayload.Status -> setupStatusMessage(payload.newStatus)
        }
    }

}