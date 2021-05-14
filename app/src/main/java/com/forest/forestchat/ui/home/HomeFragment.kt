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
package com.forest.forestchat.ui.home

import android.view.View
import androidx.fragment.app.viewModels
import com.forest.forestchat.ui.base.fragment.NavigationFragment
import com.forest.forestchat.ui.chats.ChatsViewModel
import com.forest.forestchat.ui.dashboard.DashboardViewModel

class HomeFragment : NavigationFragment() {

    private val chatsViewModel: ChatsViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels()

    private val navigationView: HomeNavigationView
        get() = view as HomeNavigationView

    override fun buildNavigationView(): View = HomeNavigationView(requireContext())

}