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
package com.forest.forestchat.ui.settings.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.forest.forestchat.R
import com.forest.forestchat.app.TransversalBusEvent
import com.forest.forestchat.extensions.observe
import com.forest.forestchat.ui.base.fragment.NavigationFragment
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SettingsAppFragment : NavigationFragment() {

    private val viewModel: SettingsAppViewModel by viewModels()

    private val navigationView: SettingsAppNavigationView
        get() = view as SettingsAppNavigationView

    override fun buildNavigationView(): View = SettingsAppNavigationView(requireContext())

    override fun getStatusBarBgColor(): Int = R.color.background

    override fun getNavigationBarBgColor(): Int = R.color.background

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(navigationView) {
            onNotifications = { showNotifications() }
            onSync = viewModel::syncData
        }

        with(viewModel) {
            observe(loading(), navigationView::updateLoading)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @Suppress("unused")
    fun onTransversalEvent(event: TransversalBusEvent) {
        // nothing
    }

    private fun showNotifications() {
        val intent = Intent()
        intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
        intent.putExtra("android.provider.extra.APP_PACKAGE", context?.packageName)
        requireActivity().startActivity(intent)
    }

}