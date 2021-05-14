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
package com.forest.forestchat.ui.base.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.forest.forestchat.extensions.closeKeyboard
import com.forest.forestchat.extensions.generateConsistentId
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
abstract class NavigationFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        buildNavigationView().also { view ->
            view.id = generateConsistentId()

            // prevents click below fragments
            view.setOnClickListener { }
        }

    override fun onPause() {
        super.onPause()
        context?.closeKeyboard(view)
    }

    /**
     * set fragment view
     */
    protected abstract fun buildNavigationView(): View

}