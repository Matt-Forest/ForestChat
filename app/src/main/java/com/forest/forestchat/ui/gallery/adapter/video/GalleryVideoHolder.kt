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
package com.forest.forestchat.ui.gallery.adapter.video

import android.view.ViewGroup
import com.forest.forestchat.R
import com.forest.forestchat.databinding.HolderVideoGalleryBinding
import com.forest.forestchat.ui.base.recycler.BaseHolder
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

class GalleryVideoHolder(parent: ViewGroup) :
    BaseHolder<GalleryVideoItem>(parent, R.layout.holder_video_gallery) {

    private val binding = HolderVideoGalleryBinding.bind(itemView)

    var player: SimpleExoPlayer? = null

    override fun bind(item: GalleryVideoItem) {
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory()
        val trackSelector = DefaultTrackSelector(context, videoTrackSelectionFactory)

        player = SimpleExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .build()
        binding.video.player = player

        val dataSourceFactory =
            DefaultDataSourceFactory(context, Util.getUserAgent(context, "ForestChat"))
        val videoSource =
            ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(item.media.uri))
        player?.setMediaSource(videoSource)
        player?.prepare()
    }

    fun destroy() {
        player?.release()
    }

}