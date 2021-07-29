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
package com.forest.forestchat.ui.recipients.adapter

import android.view.ViewGroup
import com.forest.forestchat.domain.models.Recipient
import com.forest.forestchat.ui.base.recycler.BaseAdapter
import com.forest.forestchat.ui.base.recycler.BaseHolder
import com.forest.forestchat.ui.common.mappers.buildSingleAvatar

class RecipientsAdapter(
    private val onItemSelected: (Long) -> Unit
) : BaseAdapter() {

    override fun buildViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<*> =
        RecipientHolder(parent, onItemSelected)

    fun updateRecipients(recipients: List<Recipient>) {
        val items = recipients.map { recipient ->
            RecipientItem(
                recipientId = recipient.id,
                avatarType = buildSingleAvatar(recipient.contact, false),
                name = recipient.getDisplayName(),
                numberPhone = recipient.getNumberPhone(),
                canBeAdded = recipient.contact == null
            )
        }

        submitList(items)
    }

}