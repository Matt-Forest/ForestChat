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
package com.forest.forestchat.ui.create.conversation.adapter.search

import android.view.ViewGroup
import com.forest.forestchat.R
import com.forest.forestchat.databinding.HolderCreateConversationContactSearchBinding
import com.forest.forestchat.extensions.goneIf
import com.forest.forestchat.extensions.invisibleIf
import com.forest.forestchat.ui.base.recycler.BaseHolder

class CreateConversationSearchContactHolder(
    parent: ViewGroup,
    private val onCheck: (Long) -> Unit
) : BaseHolder<CreateConversationSearchContactItem>(
    parent,
    R.layout.holder_create_conversation_contact_search
) {

    private val binding = HolderCreateConversationContactSearchBinding.bind(itemView)

    override fun bind(item: CreateConversationSearchContactItem) {
        with(binding) {
            letter.text = item.letter?.toString()
            letter.invisibleIf { item.letter == null }
            avatars.updateAvatars(item.avatarType)
            checkbox.isChecked = item.isChecked
            name.text = item.name
            numberPhone.text = item.number
            numberPhone.goneIf { item.number.isNullOrEmpty() }
            checkbox.setOnClickListener { onCheck(item.contactId) }
        }

        itemView.setOnClickListener { onCheck(item.contactId) }
    }

    fun onPayload(payload: CreateConversationSearchPayload) {
        when (payload) {
            is CreateConversationSearchPayload.CheckContact -> binding.checkbox.isChecked =
                payload.check
        }
    }

}