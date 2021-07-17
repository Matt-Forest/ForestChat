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
package com.forest.forestchat.ui.conversation.attachmentsAdapter

import android.view.ViewGroup
import com.forest.forestchat.ui.base.recycler.BaseAdapter
import com.forest.forestchat.ui.base.recycler.BaseHolder
import com.forest.forestchat.ui.base.recycler.BaseItem
import com.forest.forestchat.ui.conversation.attachmentsAdapter.contact.AttachmentContactHolder
import com.forest.forestchat.ui.conversation.attachmentsAdapter.contact.AttachmentContactItem
import com.forest.forestchat.ui.conversation.attachmentsAdapter.image.AttachmentImageHolder
import com.forest.forestchat.ui.conversation.attachmentsAdapter.image.AttachmentImageItem
import com.forest.forestchat.ui.conversation.models.Attachment
import ezvcard.Ezvcard

class AttachmentsAdapter : BaseAdapter() {

    override fun buildViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<*>? =
        when (viewType) {
            AttachmentsViewTypes.ATTACHMENT_IMAGE -> AttachmentImageHolder(parent)
            AttachmentsViewTypes.ATTACHMENT_CONTACT -> AttachmentContactHolder(parent)
            else -> null
        }

    fun setAttachments(attachments: List<Attachment>) {
        val items: List<BaseItem> = attachments.map { attachment ->
            when (attachment) {
                is Attachment.Image -> AttachmentImageItem(attachment.uri)
                is Attachment.Contact -> AttachmentContactItem(
                    Ezvcard.parse(attachment.vCard).first().formattedName.value
                )
            }
        }

        submitList(items)
    }

}