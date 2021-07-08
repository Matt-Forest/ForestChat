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

import android.content.Context
import android.view.LayoutInflater
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.forest.forestchat.R
import com.forest.forestchat.databinding.NavigationHomeBinding
import com.forest.forestchat.extensions.asColor
import com.forest.forestchat.extensions.invisibleIf
import com.forest.forestchat.ui.conversations.HomeConversationsNavigationView
import com.forest.forestchat.ui.conversations.models.HomeConversationEvent

class HomeNavigationView(context: Context) : CoordinatorLayout(context) {

    // Common
    lateinit var toggleTab: (HomeTab) -> Unit

    private var selectedTab: HomeTab = HomeTab.Conversations
    private val binding: NavigationHomeBinding

    init {
        val layoutInflater = LayoutInflater.from(context)
        binding = NavigationHomeBinding.inflate(layoutInflater, this)

        binding.fab.drawable.setTint(R.color.white.asColor(context))

        setupBottomView()
        toggleViews()
    }

    fun getConversationsView(): HomeConversationsNavigationView = binding.conversationsContainerView

    private fun setupBottomView() {
        binding.bottomNavigationView.setOnNavigationItemSelectedListener {
            val selectedId = it.itemId
            if (selectedTab.id == selectedId) {
                return@setOnNavigationItemSelectedListener false
            }

            when (selectedId) {
                HomeTab.Conversations.id -> {
                    selectedTab = HomeTab.Conversations
                    toggleViews()
                }
                HomeTab.Dashboard.id -> {
                    selectedTab = HomeTab.Dashboard
                    toggleViews()
                }
            }
            toggleTab(selectedTab)

            true
        }
    }

    private fun toggleViews() {
        with(binding) {
            conversationsContainerView.invisibleIf { selectedTab == HomeTab.Dashboard }
            dashboardContainerView.invisibleIf { selectedTab == HomeTab.Conversations }
        }
    }

}