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
package com.forest.forestchat.ui.conversation.attachmentsAdapter.image

import android.view.ViewGroup
import com.forest.forestchat.R
import com.forest.forestchat.databinding.HolderAttachmentImageBinding
import com.forest.forestchat.extensions.ImageSignatureKeys
import com.forest.forestchat.extensions.loadUri
import com.forest.forestchat.ui.base.recycler.BaseHolder

class AttachmentImageHolder(
    parent: ViewGroup,
    private val onRemove: (Int) -> Unit
) : BaseHolder<AttachmentImageItem>(parent, R.layout.holder_attachment_image) {

    private val binding = HolderAttachmentImageBinding.bind(itemView)

    override fun bind(item: AttachmentImageItem) {
        with(binding) {
            image.loadUri(uri = item.uri, imageSignatureKeys = ImageSignatureKeys.AttachmentImage)
            remove.setOnClickListener { onRemove(item.index) }
        }
    }

}