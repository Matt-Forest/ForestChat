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
package com.forest.forestchat.ui.conversation.adapter.messageRecipientMedias

import android.view.ViewGroup
import coil.load
import com.forest.forestchat.R
import com.forest.forestchat.databinding.HolderMessageRecipientMediaBinding
import com.forest.forestchat.extensions.visible
import com.forest.forestchat.extensions.visibleIf
import com.forest.forestchat.ui.base.recycler.BaseHolder
import com.forest.forestchat.ui.common.media.Media
import com.forest.forestchat.ui.common.media.MediaView

class MessageRecipientMediasHolder(
    parent: ViewGroup
) : BaseHolder<MessageRecipientMediasItem>(parent, R.layout.holder_message_recipient_media) {

    private val binding = HolderMessageRecipientMediaBinding.bind(itemView)

    override fun bind(item: MessageRecipientMediasItem) {
        with(binding) {
            date.text = item.date
            date.visibleIf { item.date != null }
            info.text = item.hours
            name.text = item.name
            avatar.setAvatar(item.avatarType)

            setMedias(item.medias)

            itemView.setOnClickListener {
                info.visible()
            }
        }
    }

    private fun setMedias(medias: List<Media>) {
        val maxItemByRow = 3
        val isAlone = medias.size == 1
        val isOnOneRow = medias.size <= maxItemByRow

        val mediasView = mutableListOf<MediaView>()

        medias.forEachIndexed { index, media ->
            when {
                isAlone -> mediasView.add(toMediaViewAlone(media))
                isOnOneRow -> mediasView.add(toMediaViewOnOneRow(index, media))
                else -> mediasView.add(toMediaView(index, medias.size, media))
            }
        }
    }

    private fun toMediaViewAlone(media: Media): MediaView =
        buildMediaView(MediaView.RoundedStyle.Alone, media)

    private fun toMediaViewOnOneRow(index: Int, media: Media): MediaView =
        when (index) {
            0 -> buildMediaView(MediaView.RoundedStyle.Left, media)
            1 -> buildMediaView(MediaView.RoundedStyle.Middle, media)
            else -> buildMediaView(MediaView.RoundedStyle.Right, media)
        }

    private fun toMediaView(index: Int, size: Int, media: Media): MediaView {
        val isOnFirstRow = index < 3
        val numberOfElementOnLastRow = when (size % 3 == 0) {
            true -> 3
            false -> size % 3
        }
        val isOnLastRow = index > size - numberOfElementOnLastRow - 1
        val indexOnRow = when (index % 3 == 0) {
            true -> 3
            false -> index % 3
        }

        return when {
            isOnFirstRow ->
                when (indexOnRow) {
                    1 -> buildMediaView(MediaView.RoundedStyle.TopLeft, media)
                    2 -> buildMediaView(MediaView.RoundedStyle.Middle, media)
                    else -> buildMediaView(MediaView.RoundedStyle.TopRight, media)
                }

            isOnLastRow ->
                when (indexOnRow) {
                    1 -> buildMediaView(MediaView.RoundedStyle.BottomLeft, media)
                    2 -> buildMediaView(MediaView.RoundedStyle.Middle, media)
                    else -> buildMediaView(MediaView.RoundedStyle.BottomRight, media)
                }
            else -> buildMediaView(MediaView.RoundedStyle.Middle, media)
        }
    }

    private fun buildMediaView(style: MediaView.RoundedStyle, media: Media): MediaView =
        MediaView(context).apply {
            setStyle(style, media.isVideo)
            load(media.uri)
        }

}