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
package com.forest.forestchat.ui.conversation

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import com.forest.forestchat.R
import com.forest.forestchat.databinding.NavigationConversationBinding
import com.forest.forestchat.extensions.gone
import com.forest.forestchat.extensions.visible
import com.forest.forestchat.extensions.visibleIf
import com.forest.forestchat.ui.conversation.adapter.ConversationAdapter
import com.forest.forestchat.ui.conversation.adapter.MessageItemEvent
import com.forest.forestchat.ui.conversation.dialog.MessageOptionType
import com.forest.forestchat.ui.conversation.dialog.MessageOptionsDialog
import com.forest.forestchat.ui.conversation.models.ConversationEvent
import com.forest.forestchat.ui.conversation.models.ConversationState
import com.forest.forestchat.ui.gallery.GalleryInput

class ConversationNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding: NavigationConversationBinding

    lateinit var onMessageEvent: (MessageItemEvent) -> Unit
    lateinit var optionSelected: (MessageOptionType) -> Unit

    private var conversationAdapter = ConversationAdapter(context) { onMessageEvent(it) }

    init {
        val layoutInflater = LayoutInflater.from(context)
        binding = NavigationConversationBinding.inflate(layoutInflater, this)

        binding.back.setOnClickListener { findNavController().popBackStack() }
    }

    fun event(event: ConversationEvent) {
        when (event) {
            is ConversationEvent.ShowMessageOptions -> {
                MessageOptionsDialog(context, event.canCopy) { optionSelected(it) }
                    .create()
                    .show()
            }
            is ConversationEvent.ShowMessageDetails -> {
                AlertDialog.Builder(context)
                    .setTitle(R.string.message_details_title)
                    .setMessage(event.details)
                    .setCancelable(true)
                    .show()
            }
            is ConversationEvent.ShowGallery -> {
                val input = GalleryInput(event.medias, event.mediaSelected)
                findNavController().navigate(ConversationFragmentDirections.goToGallery(input))
            }
            else -> null
        }
    }

    fun setLoading(isVisible: Boolean) {
        binding.loading.visibleIf { isVisible }
    }

    fun updateState(state: ConversationState) {
        with(binding) {
            when (state) {
                is ConversationState.Empty -> {
                    emptyAvatars.updateAvatars(state.avatarType)
                    emptyLabel.text = state.title
                    emptyPhone.text = state.phone
                    recyclerConversation.gone()
                    emptyContainer.visible()
                }
                is ConversationState.Data -> {
                    if (recyclerConversation.adapter !== conversationAdapter) {
                        recyclerConversation.adapter = conversationAdapter
                    }
                    conversationAdapter.apply {
                        setMessages(state.messages, state.recipients, state.subscriptionsInfo)
                    }
                    emptyContainer.gone()
                    recyclerConversation.visible()
                }
            }
        }
    }

    fun updateTitle(title: String) {
        binding.conversationTitle.text = title
    }

}