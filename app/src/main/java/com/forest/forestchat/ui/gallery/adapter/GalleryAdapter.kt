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
package com.forest.forestchat.ui.gallery.adapter

import android.view.ViewGroup
import com.forest.forestchat.ui.base.recycler.BaseHolder
import com.forest.forestchat.ui.base.recycler.BasePagerAdapter
import com.forest.forestchat.ui.common.media.Media
import com.forest.forestchat.ui.gallery.adapter.image.GalleryImageHolder
import com.forest.forestchat.ui.gallery.adapter.image.GalleryImageItem
import com.forest.forestchat.ui.gallery.adapter.video.GalleryVideoHolder
import com.forest.forestchat.ui.gallery.adapter.video.GalleryVideoItem

class GalleryAdapter : BasePagerAdapter() {

    override fun buildViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<*>? =
        when (viewType) {
            GalleryViewTypes.GALLERY_IMAGE -> GalleryImageHolder(parent)
            GalleryViewTypes.GALLERY_VIDEO -> GalleryVideoHolder(parent)
            else -> null
        }

    fun setData(medias: List<Media>) {
        val items = medias.map { media ->
            when (media.isVideo) {
                true -> GalleryVideoItem(media)
                false -> GalleryImageItem(media)
            }
        }
        submitList(items)
    }

    fun destroy() {
        holders.filter { it.itemViewType == GalleryViewTypes.GALLERY_VIDEO }.forEach { holder ->
            (holder as GalleryVideoHolder).destroy()
        }
    }

}