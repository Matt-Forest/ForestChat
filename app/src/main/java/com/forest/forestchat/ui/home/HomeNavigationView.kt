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
import com.forest.forestchat.ui.chats.ChatsEvent

class HomeNavigationView(context: Context) : CoordinatorLayout(context) {

    // Conversations view
    lateinit var requestSmsPermissionChats: () -> Unit
    lateinit var onSearchChangedChats: (String) -> Unit

    // Common
    lateinit var toggleTab: (HomeTab) -> Unit

    private var selectedTab: HomeTab = HomeTab.Chats
    private val binding: NavigationHomeBinding

    init {
        val layoutInflater = LayoutInflater.from(context)
        binding = NavigationHomeBinding.inflate(layoutInflater, this)

        binding.fab.drawable.setTint(R.color.white.asColor(context))

        setupChatsView()
        setupBottomView()
        toggleViews()
    }

    private fun setupChatsView() {
        with(binding.chatsContainerView) {
            requestSmsPermission = { requestSmsPermissionChats() }
            onSearchChange = { onSearchChangedChats(it) }
        }
    }

    fun event(event: HomeEvent) {
        binding.chatsContainerView.event(
            when (event) {
                HomeEvent.RequestDefaultSms,
                HomeEvent.RequestPermission -> ChatsEvent.NeedPermission
                HomeEvent.ChatsLoading -> ChatsEvent.Loading
                HomeEvent.NoConversations -> ChatsEvent.NoData
                is HomeEvent.ConversationsData -> ChatsEvent.ConversationsData(event.conversations)
                HomeEvent.NoSearchData -> ChatsEvent.NoSearchData
                is HomeEvent.Search -> ChatsEvent.SearchData(event.conversations, event.contacts)
            }
        )
    }

    private fun setupBottomView() {
        binding.bottomNavigationView.setOnNavigationItemSelectedListener {
            val selectedId = it.itemId
            if (selectedTab.id == selectedId) {
                return@setOnNavigationItemSelectedListener false
            }

            when (selectedId) {
                HomeTab.Chats.id -> {
                    selectedTab = HomeTab.Chats
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
            chatsContainerView.invisibleIf { selectedTab == HomeTab.Dashboard }
            dashboardContainerView.invisibleIf { selectedTab == HomeTab.Chats }
        }
    }

}