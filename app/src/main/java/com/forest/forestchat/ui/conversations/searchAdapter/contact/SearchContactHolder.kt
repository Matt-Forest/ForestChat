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
package com.forest.forestchat.ui.conversations.searchAdapter.contact

import android.view.ViewGroup
import com.forest.forestchat.R
import com.forest.forestchat.databinding.HolderContactSearchBinding
import com.forest.forestchat.ui.base.recycler.BaseHolder

class SearchContactHolder(
    parent: ViewGroup,
    private val onClick: (Long) -> Unit
) : BaseHolder<SearchContactItem>(parent, R.layout.holder_contact_search) {

    private val binding = HolderContactSearchBinding.bind(itemView)

    override fun bind(item: SearchContactItem) {
        with(binding) {
            avatars.updateAvatars(item.avatarType)
            title.text = item.name
            snippet.text = item.number

            itemView.setOnClickListener { onClick(item.id) }
        }
    }

}