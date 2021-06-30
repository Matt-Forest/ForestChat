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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import com.forest.forestchat.databinding.NavigationConversationBinding
import com.forest.forestchat.ui.conversation.adapter.ConversationAdapter

class ConversationNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding: NavigationConversationBinding

    private var conversationAdapter = ConversationAdapter(context)

    init {
        val layoutInflater = LayoutInflater.from(context)
        binding = NavigationConversationBinding.inflate(layoutInflater, this)

        binding.back.setOnClickListener { findNavController().popBackStack() }
    }

    fun event(event: ConversationEvent) {
        with(binding) {
            when (event) {
                ConversationEvent.Empty -> {

                }
                ConversationEvent.Loading -> {

                }
                is ConversationEvent.BaseData -> conversationTitle.text = event.title
                is ConversationEvent.Data -> {
                    if (recyclerConversation.adapter !== conversationAdapter) {
                        recyclerConversation.adapter = conversationAdapter
                    }
                    conversationAdapter.apply {
                        setMessages(event.messages, event.recipients)
                    }
                }
            }
        }
    }

}