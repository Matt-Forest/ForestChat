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

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.navigation.findNavController
import com.forest.forestchat.databinding.NavigationSettingsArchiveBinding
import com.forest.forestchat.domain.models.Conversation
import com.forest.forestchat.extensions.gone
import com.forest.forestchat.extensions.visible
import com.forest.forestchat.ui.common.conversations.adapter.ConversationsAdapter
import com.forest.forestchat.ui.common.conversations.adapter.conversation.ConversationItemEvent
import com.forest.forestchat.ui.common.conversations.dialog.ConversationOptionType
import com.forest.forestchat.ui.common.conversations.dialog.ConversationOptionsDialog
import com.forest.forestchat.ui.common.dialog.ConversationDeleteDialog
import com.forest.forestchat.ui.conversation.models.ConversationInput
import com.forest.forestchat.ui.conversations.models.HomeConversationEvent
import com.forest.forestchat.ui.home.HomeFragmentDirections
import com.forest.forestchat.ui.settings.archives.models.SettingsArchiveEvent
import com.forest.forestchat.ui.settings.archives.models.SettingsArchiveState

class SettingsArchiveNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    lateinit var onConversationEvent: (ConversationItemEvent) -> Unit
    lateinit var optionSelected: (ConversationOptionType) -> Unit
    lateinit var onConversationDeleted: (Long) -> Unit

    private val binding: NavigationSettingsArchiveBinding
    private var conversationsAdapter = ConversationsAdapter { onConversationEvent(it) }

    init {
        val layoutInflater = LayoutInflater.from(context)
        binding = NavigationSettingsArchiveBinding.inflate(layoutInflater, this)

        orientation = VERTICAL

        with(binding) {
            back.setOnClickListener { findNavController().popBackStack() }
        }
    }

    fun event(event: SettingsArchiveEvent) {
        when (event) {
            is SettingsArchiveEvent.GoToConversation -> {
                val input = ConversationInput(event.conversation)
                findNavController().navigate(SettingsArchiveFragmentDirections.goToConversation(input))
            }
            is SettingsArchiveEvent.ShowConversationOptions -> {
                ConversationOptionsDialog(
                    context,
                    { optionSelected(it) },
                    showAddToContacts = false,
                    showPin = false,
                    showPinnedOff = false,
                    showMarkAsRead = false,
                    showUnarchived = true
                ).create().show()
            }
            is SettingsArchiveEvent.RequestDeleteDialog -> {
                ConversationDeleteDialog(context) { onConversationDeleted(event.id) }
                    .create()
                    .show()
            }
        }
    }

    fun updateState(state: SettingsArchiveState) {
        with(binding) {
            when (state) {
                SettingsArchiveState.Empty -> {
                    recyclerArchive.gone()
                    empty.visible()
                }
                is SettingsArchiveState.Conversations -> {
                    if (recyclerArchive.adapter !== conversationsAdapter) {
                        recyclerArchive.adapter = conversationsAdapter
                    }
                    recyclerArchive.visible()
                    empty.gone()
                    conversationsAdapter.apply {
                        setConversations(context, state.conversations, true)
                    }
                }
            }
        }
    }

    fun updateLoader(isLoad: Boolean) {
        with(binding.loadBar) {
            when (isLoad) {
                true -> startLoading()
                false -> stopLoading()
            }
        }
    }

}