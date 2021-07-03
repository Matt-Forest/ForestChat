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
package com.forest.forestchat.ui.conversation.adapter.messageMedias.user

import android.view.ViewGroup
import androidx.core.view.isGone
import com.forest.forestchat.R
import com.forest.forestchat.databinding.HolderMessageUserMediaBinding
import com.forest.forestchat.extensions.visibleIf
import com.forest.forestchat.ui.conversation.adapter.messageMedias.MessageMediasBaseHolder

class MessageUserMediasHolder(
    parent: ViewGroup
) : MessageMediasBaseHolder<MessageUserMediasItem>(parent, R.layout.holder_message_user_media) {

    private val binding = HolderMessageUserMediaBinding.bind(itemView)

    override fun bind(item: MessageUserMediasItem) {
        with(binding) {
            date.text = item.date
            date.visibleIf { item.date != null }
            info.text = item.hours

            setMedias(medias, item.medias)

            itemView.setOnClickListener {
                info.visibleIf { info.isGone }
            }
        }
    }

}