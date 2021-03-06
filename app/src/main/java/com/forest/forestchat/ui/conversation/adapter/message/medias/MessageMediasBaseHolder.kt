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
package com.forest.forestchat.ui.conversation.adapter.message.medias

import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import androidx.annotation.LayoutRes
import coil.size.PixelSize
import coil.size.Size
import com.forest.forestchat.extensions.ImageSignatureKeys
import com.forest.forestchat.extensions.dp
import com.forest.forestchat.extensions.loadUri
import com.forest.forestchat.ui.base.recycler.BaseHolder
import com.forest.forestchat.ui.common.media.Media
import com.forest.forestchat.ui.common.media.MediaView
import com.forest.forestchat.ui.conversation.adapter.MessageItemEvent

abstract class MessageMediasBaseHolder<T>(
    parent: ViewGroup,
    private val onEvent: (MessageItemEvent) -> Unit,
    @LayoutRes layoutRes: Int
) : BaseHolder<T>(parent, layoutRes) {

    protected fun setMedias(tableMedias: TableLayout, medias: List<Media>, tableWidth: Float) {
        val isAlone = medias.size == 1
        val isOnOneRow = medias.size <= 3

        val mediasView = medias.mapIndexed { index, media ->
            when {
                isAlone -> toMediaViewAlone(media)
                isOnOneRow -> toMediaViewOnOneRow(index, media, medias.size == 3)
                else -> toMediaView(index, medias.size, media)
            }
        }

        setMediasInTable(tableMedias, mediasView, tableWidth)
    }

    private fun setMediasInTable(
        tableMedias: TableLayout,
        mediasView: List<MediaView>,
        tableWidth: Float
    ) {
        tableMedias.removeAllViews()
        mediasView.chunked(3).forEach { mediasRow ->
            val rowView = TableRow(context)
            val lp = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
            rowView.layoutParams = lp
            mediasRow.forEach { mediaView ->
                rowView.addView(mediaView)
                mediaView.setSize(tableWidth, mediasRow.size)
            }
            rowView.setPadding(0, 1.dp, 0, 1.dp)
            tableMedias.addView(rowView)
        }
    }

    private fun toMediaViewAlone(media: Media): MediaView =
        buildMediaView(MediaView.RoundedStyle.Alone, media)

    private fun toMediaViewOnOneRow(index: Int, media: Media, rowIsFull: Boolean): MediaView =
        when (index) {
            0 -> buildMediaView(MediaView.RoundedStyle.Left, media)
            1 -> buildMediaView(
                when (rowIsFull) {
                    true -> MediaView.RoundedStyle.Middle
                    false -> MediaView.RoundedStyle.Right
                },
                media
            )
            else -> buildMediaView(MediaView.RoundedStyle.Right, media)
        }

    private fun toMediaView(index: Int, size: Int, media: Media): MediaView {
        val isOnFirstRow = index < 3
        val numberOfElementOnLastRow = when (size % 3 == 0) {
            true -> 3
            false -> size % 3
        }
        val isOnLastRow = index > size - numberOfElementOnLastRow - 1
        val indexOnRow = index % 3

        return when {
            isOnFirstRow ->
                when (indexOnRow) {
                    0 -> buildMediaView(MediaView.RoundedStyle.TopLeft, media)
                    1 -> buildMediaView(MediaView.RoundedStyle.Middle, media)
                    else -> buildMediaView(MediaView.RoundedStyle.TopRight, media)
                }
            isOnLastRow ->
                when (indexOnRow) {
                    0 -> buildMediaView(MediaView.RoundedStyle.BottomLeft, media)
                    1 -> buildMediaView(MediaView.RoundedStyle.Middle, media)
                    else -> buildMediaView(MediaView.RoundedStyle.BottomRight, media)
                }
            else -> buildMediaView(MediaView.RoundedStyle.Middle, media)
        }
    }

    private fun buildMediaView(
        style: MediaView.RoundedStyle,
        media: Media
    ): MediaView = MediaView(context).apply {
        when (media.isGif) {
            true -> loadUri(
                uri = media.uri,
                size = PixelSize(180.dp, 180.dp),
                imageSignatureKeys = ImageSignatureKeys.Conversation.Message
            )
            false -> loadUri(
                uri = media.uri,
                imageSignatureKeys = ImageSignatureKeys.Conversation.Message
            )
        }
        setStyle(style, media.isVideo)

        setOnClickListener { onEvent(MessageItemEvent.MediaSelected(media.mediaId)) }
    }

}