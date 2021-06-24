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
package com.forest.forestchat.ui.conversation.adapter.messageUserMiddle

import android.view.ViewGroup
import com.forest.forestchat.R
import com.forest.forestchat.databinding.HolderMessageUserMiddleBinding
import com.forest.forestchat.extensions.visible
import com.forest.forestchat.ui.base.recycler.BaseHolder

class MessageUserMiddleHolder(
    parent: ViewGroup
) : BaseHolder<MessageUserMiddleItem>(parent, R.layout.holder_message_user_middle) {

    private val binding = HolderMessageUserMiddleBinding.bind(itemView)

    override fun bind(item: MessageUserMiddleItem) {
        with(binding) {
            message.text = item.message
            info.text = item.hours

            itemView.setOnClickListener {
                info.visible()
            }
        }
    }

}