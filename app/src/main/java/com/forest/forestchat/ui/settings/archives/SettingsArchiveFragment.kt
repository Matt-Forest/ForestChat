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
package com.forest.forestchat.ui.settings.archives

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.forest.forestchat.R
import com.forest.forestchat.extensions.observe
import com.forest.forestchat.ui.base.fragment.NavigationFragment
import com.zhuinden.liveevent.observe

class SettingsArchiveFragment : NavigationFragment() {

    private val viewModel: SettingsArchiveViewModel by viewModels()

    private val navigationView: SettingsArchiveNavigationView
        get() = view as SettingsArchiveNavigationView

    override fun buildNavigationView(): View = SettingsArchiveNavigationView(requireContext())

    override fun getStatusBarBgColor(): Int = R.color.toolbarBackground

    override fun getNavigationBarBgColor(): Int = R.color.background

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(navigationView) {
            onConversationEvent = viewModel::onConversationEvent
            optionSelected = viewModel::conversationOptionSelected
            onConversationDeleted = viewModel::removeConversation
        }

        with(viewModel) {
            observe(isLoading(), navigationView::updateLoader)
            observe(state(), navigationView::updateState)
            eventSource().observe(viewLifecycleOwner, navigationView::event)
        }
    }

}