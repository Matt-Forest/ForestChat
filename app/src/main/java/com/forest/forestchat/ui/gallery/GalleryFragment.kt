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

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import com.forest.forestchat.R
import com.forest.forestchat.ui.base.fragment.FullscreenLifecycle
import com.forest.forestchat.ui.base.fragment.NavigationFragment
import com.forest.forestchat.ui.base.fragment.OrientationLifecycle

class GalleryFragment : NavigationFragment() {

    private val navigationView: GalleryNavigationView
        get() = view as GalleryNavigationView

    private val fullscreenLifecycle by lazy { FullscreenLifecycle(this) }
    private val orientationLifecycle by lazy {
        OrientationLifecycle(
            this,
            ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        )
    }

    override fun buildNavigationView(): View = GalleryNavigationView(requireContext())

    override fun getStatusBarBgColor(): Int = R.color.toolbarBackground

    override fun getNavigationBarBgColor(): Int = R.color.background


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(viewLifecycleOwner.lifecycle) {
            addObserver(fullscreenLifecycle)
            addObserver(orientationLifecycle)
        }
    }

    override fun onDestroyView() {
        navigationView.onDestroy()
        with(viewLifecycleOwner.lifecycle) {
            removeObserver(fullscreenLifecycle)
            removeObserver(orientationLifecycle)
        }

        super.onDestroyView()
    }

}