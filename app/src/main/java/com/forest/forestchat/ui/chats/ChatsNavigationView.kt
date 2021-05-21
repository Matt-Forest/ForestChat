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
package com.forest.forestchat.ui.chats

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import com.forest.forestchat.databinding.NavigationChatsBinding
import com.forest.forestchat.extensions.gone
import com.forest.forestchat.extensions.visible
import com.forest.forestchat.ui.chats.adapter.ConversationsAdapter

class ChatsNavigationView : ConstraintLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    lateinit var requestSmsPermission: () -> Unit

    private val binding: NavigationChatsBinding

    init {
        val layoutInflater = LayoutInflater.from(context)
        binding = NavigationChatsBinding.inflate(layoutInflater, this)

        binding.changePermission.setOnClickListener { requestSmsPermission() }
        binding.userProfile.setOnClickListener {
            when (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO) {
                true -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                false -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    fun event(event: ChatsEvent) = when (event) {
        ChatsEvent.NeedPermission -> {
            binding.empty.gone()
            binding.recyclerChat.gone()
            binding.requestPermission.visible()
        }
        ChatsEvent.NoData -> {
            binding.empty.visible()
            binding.recyclerChat.gone()
            binding.requestPermission.gone()
        }
        is ChatsEvent.ConversationsData -> {
            binding.empty.gone()
            binding.recyclerChat.visible()
            binding.requestPermission.gone()

            val adapter = ConversationsAdapter().apply {
                    setConversations(context, event.conversations)
                }
            binding.recyclerChat.adapter = adapter
        }
    }

}