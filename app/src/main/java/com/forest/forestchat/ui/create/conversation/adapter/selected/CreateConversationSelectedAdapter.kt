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
package com.forest.forestchat.ui.create.conversation.adapter.selected

import android.view.ViewGroup
import com.forest.forestchat.domain.models.contact.Contact
import com.forest.forestchat.ui.base.recycler.BaseAdapter
import com.forest.forestchat.ui.base.recycler.BaseHolder
import com.forest.forestchat.ui.common.avatar.AvatarType
import com.forest.forestchat.ui.common.mappers.buildSingleAvatar
import com.forest.forestchat.ui.create.conversation.models.ContactSelected

class CreateConversationSelectedAdapter(
    private val onRemove: (Int) -> Unit
) : BaseAdapter() {

    override fun buildViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<*> =
        CreateConversationSelectedHolder(parent, onRemove)

    fun update(data: List<ContactSelected>) {
        val items = data.mapIndexed { index, contactSelected ->
            CreateConversationSelectedItem(
                index = index,
                name = contactSelected.contact?.let { buildName(it) }
                    ?: contactSelected.newAddress
                    ?: "",
                avatarType = contactSelected.contact?.let { buildSingleAvatar(it, false) }
                    ?: AvatarType.Single.Profile
            )
        }
        submitList(items)
    }

    private fun buildName(contact: Contact): String =
        contact.name ?: buildAddress(contact)

    private fun buildAddress(contact: Contact): String =
        (contact.getDefaultNumber() ?: contact.numbers.first()).address

}