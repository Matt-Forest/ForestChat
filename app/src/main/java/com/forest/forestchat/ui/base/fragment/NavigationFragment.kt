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

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import com.forest.forestchat.app.TransversalBusEvent
import com.forest.forestchat.extensions.asColor
import com.forest.forestchat.extensions.closeKeyboard
import com.forest.forestchat.extensions.generateConsistentId
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@AndroidEntryPoint
abstract class NavigationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        buildNavigationView().also { view ->
            view.id = generateConsistentId()

            // prevents click below fragments
            view.setOnClickListener { }
        }

    override fun onResume() {
        super.onResume()
        updateStatusBarMode()
        updateNavigationBar()
    }

    override fun onPause() {
        super.onPause()
        context?.closeKeyboard(view)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @Suppress("unused")
    open fun onTransversalEvent(event: TransversalBusEvent) {
        // nothing
    }

    /**
     * set fragment view
     */
    protected abstract fun buildNavigationView(): View

    /**
     * Get color of status bar
     */
    protected abstract fun getStatusBarBgColor(): Int

    /**
     * Get color of navigation bar
     */
    protected abstract fun getNavigationBarBgColor(): Int

    @Suppress("DEPRECATION")
    fun updateStatusBarMode() {
        val isDarkColor = isDarkColor(getStatusBarBgColor().asColor(context))
        with(requireActivity().window) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                var appearance = insetsController?.systemBarsAppearance
                    ?: WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                appearance = when (isDarkColor) {
                    true -> appearance and WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS.inv()
                    false -> appearance or WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                }
                insetsController?.setSystemBarsAppearance(appearance, appearance)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                var flags = decorView.systemUiVisibility
                flags = when (isDarkColor) {
                    true -> flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                    false -> flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
                decorView.systemUiVisibility = flags
            }

            statusBarColor = getStatusBarBgColor().asColor(context)
        }
    }

    @Suppress("DEPRECATION")
    fun updateNavigationBar() {
        val isDarkColor = isDarkColor(getNavigationBarBgColor().asColor(context))
        with(requireActivity().window) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                var appearance = insetsController?.systemBarsAppearance
                    ?: WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                appearance = when (isDarkColor) {
                    true -> appearance and WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS.inv()
                    false -> appearance or WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                }
                insetsController?.setSystemBarsAppearance(appearance, appearance)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                var flags = decorView.systemUiVisibility
                flags = when (isDarkColor) {
                    true -> flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                    false -> flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
                decorView.systemUiVisibility = flags
            }

            navigationBarColor = getNavigationBarBgColor().asColor(context)
        }
    }

    private fun isDarkColor(color: Int): Boolean = ColorUtils.calculateLuminance(color) < 0.5

}