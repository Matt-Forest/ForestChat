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
package com.forest.forestchat.ui.createConversation.adapter.selected

import android.view.ViewGroup
import com.forest.forestchat.R
import com.forest.forestchat.databinding.HolderCreateConversationContactSelectedBinding
import com.forest.forestchat.ui.base.recycler.BaseHolder

class CreateConversationSelectedHolder(
    parent: ViewGroup,
    private val onRemove: (Int) -> Unit
) : BaseHolder<CreateConversationSelectedItem>(
    parent,
    R.layout.holder_create_conversation_contact_selected
) {

    private val binding = HolderCreateConversationContactSelectedBinding.bind(itemView)

    override fun bind(item: CreateConversationSelectedItem) {
        with(binding) {
            name.text = item.name
            avatar.setAvatar(item.avatarType)
        }
        itemView.setOnClickListener { onRemove(item.index) }
    }

}