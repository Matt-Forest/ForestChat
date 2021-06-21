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

import android.graphics.Typeface
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.forest.forestchat.R
import com.forest.forestchat.databinding.HolderConversationBinding
import com.forest.forestchat.extensions.asColor
import com.forest.forestchat.extensions.asFont
import com.forest.forestchat.ui.base.recycler.BaseHolder
import com.forest.forestchat.ui.conversations.adapter.ConversationsPayload

class ConversationHolder(
    parent: ViewGroup,
    private val onEvent: (ConversationItemEvent) -> Unit
) : BaseHolder<ConversationItem>(parent, R.layout.holder_conversation) {

    private val binding = HolderConversationBinding.bind(itemView)

    override fun bind(item: ConversationItem) {
        with(binding) {
            avatars.updateAvatars(item.avatarType)
            title.text = item.title
            snippet.text = item.lastMessage
            date.text = item.date
            chip.isVisible = item.draft
        }
        updatePin(item.pinned)
        updateMarkAsRead(item.unread)

        itemView.setOnClickListener {
            onEvent(ConversationItemEvent.Clicked(item.id))
        }

        itemView.setOnLongClickListener {
            onEvent(ConversationItemEvent.Selected(item.id))
            true
        }
    }

    private fun updateMarkAsRead(unread: Boolean) {
        binding.unread.isVisible = unread
        with(binding) {
            snippet.setTextColor(
                when (unread) {
                    true -> R.color.text.asColor(context)
                    false -> R.color.text_50.asColor(context)
                }
            )
            title.typeface = updateTypeFace(unread)
            date.typeface = updateTypeFace(unread)
        }
    }

    private fun updateTypeFace(unread: Boolean): Typeface? = when (unread) {
        true -> R.font.mulish_bold.asFont(context)
        false -> R.font.mulish_regular.asFont(context)
    }

    private fun updatePin(pinned: Boolean) {
        binding.pinned.isVisible = pinned
    }

    private fun updateTitle(newTitle: String) {
        binding.title.text = newTitle
    }

    fun onPayload(payload: ConversationsPayload) {
        when (payload) {
            is ConversationsPayload.Title -> updateTitle(payload.newTitle)
            is ConversationsPayload.Pin -> updatePin(payload.pin)
            ConversationsPayload.MarkAsRead -> updateMarkAsRead(false)
        }
    }

}