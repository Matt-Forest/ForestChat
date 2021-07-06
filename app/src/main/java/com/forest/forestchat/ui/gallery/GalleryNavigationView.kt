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
package com.forest.forestchat.ui.gallery

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.navigation.findNavController
import com.forest.forestchat.databinding.NavigationGalleryBinding
import com.forest.forestchat.extensions.addStatusBarHeightAsPadding
import com.forest.forestchat.extensions.getNavigationInput
import com.forest.forestchat.ui.gallery.adapter.GalleryAdapter

class GalleryNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val binding: NavigationGalleryBinding

    private val pagerAdapter = GalleryAdapter()

    init {
        val layoutInflater = LayoutInflater.from(context)
        binding = NavigationGalleryBinding.inflate(layoutInflater, this)

        binding.close.setOnClickListener { findNavController().popBackStack() }

        // We need to setup everything after view has been added to his parent
        // to use getNavigationInput extension which rely on findFragment()
        post {
            addStatusBarHeightAsPadding()
            val input = getNavigationInput<GalleryInput>()

            with(binding.pager) {
                adapter = pagerAdapter
                offscreenPageLimit = 1
            }
            val medias = input.medias.reversed()
            pagerAdapter.setData(medias)
            medias.takeIf { pagerAdapter.itemCount > 0 }
                ?.indexOfFirst { media -> media.mediaId == input.mediaSelected.mediaId }
                ?.let { index ->
                    binding.pager.setCurrentItem(index, false)
                }
        }
    }

    fun onDestroy() {
        pagerAdapter.destroy()
    }

}