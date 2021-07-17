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
package com.forest.forestchat.ui.conversation.attachmentsAdapter.contact

import android.view.ViewGroup
import com.forest.forestchat.R
import com.forest.forestchat.databinding.HolderAttachmentContactBinding
import com.forest.forestchat.ui.base.recycler.BaseHolder
import com.forest.forestchat.ui.common.avatar.AvatarType

class AttachmentContactHolder(
    parent: ViewGroup,
) : BaseHolder<AttachmentContactItem>(parent, R.layout.holder_attachment_contact) {

    private val binding = HolderAttachmentContactBinding.bind(itemView)

    override fun bind(item: AttachmentContactItem) {
        with(binding) {
            contactAvatar.setAvatar(AvatarType.Single.Profile)
            contactName.text = item.contactName
        }
    }

}