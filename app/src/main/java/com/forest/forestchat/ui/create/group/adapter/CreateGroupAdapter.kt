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
package com.forest.forestchat.ui.create.group.adapter

import android.view.ViewGroup
import com.forest.forestchat.domain.models.contact.Contact
import com.forest.forestchat.ui.base.recycler.BaseAdapter
import com.forest.forestchat.ui.base.recycler.BaseHolder
import com.forest.forestchat.ui.common.avatar.AvatarType
import com.forest.forestchat.ui.common.mappers.buildSingleAvatar
import com.forest.forestchat.ui.create.group.models.CreateGroupInput

class CreateGroupAdapter : BaseAdapter() {

    override fun buildViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<*> =
        CreateGroupHolder(parent)

    fun updateData(input: CreateGroupInput) {
        val items = (input.newRecipient?.map { member ->
            CreateGroupItem(
                avatarType = AvatarType.Single.Profile,
                name = member,
                numberPhone = null
            )
        } ?: listOf()) + (input.contacts?.map { contact ->
            CreateGroupItem(
                avatarType = buildSingleAvatar(contact, false),
                name = buildName(contact),
                numberPhone = buildAddress(contact)
            )
        } ?: listOf())
        submitList(items)
    }

    private fun buildName(contact: Contact): String =
        contact.name ?: buildAddress(contact)

    private fun buildAddress(contact: Contact): String =
        (contact.getDefaultNumber() ?: contact.numbers.first()).address

}