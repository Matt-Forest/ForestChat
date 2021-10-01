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
import com.forest.forestchat.R
import com.forest.forestchat.databinding.HolderRecipientBinding
import com.forest.forestchat.extensions.visibleIf
import com.forest.forestchat.ui.base.recycler.BaseHolder

class RecipientHolder(
    parent: ViewGroup,
    private val onClick: (Long) -> Unit
) : BaseHolder<RecipientItem>(parent, R.layout.holder_recipient) {

    private val binding = HolderRecipientBinding.bind(itemView)

    override fun bind(item: RecipientItem) {
        with(binding) {
            avatars.updateAvatars(item.avatarType)
            name.text = item.name
            phone.text = item.numberPhone
            addToContact.visibleIf { item.canBeAdded }

            itemView.setOnClickListener { onClick(item.recipientId) }
        }
    }

}