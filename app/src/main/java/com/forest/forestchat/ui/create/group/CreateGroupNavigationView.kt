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
package com.forest.forestchat.ui.create.group

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.findNavController
import com.forest.forestchat.R
import com.forest.forestchat.databinding.NavigationCreateGroupBinding
import com.forest.forestchat.extensions.asPlurals
import com.forest.forestchat.extensions.getNavigationInput
import com.forest.forestchat.extensions.visibleIf
import com.forest.forestchat.ui.conversation.models.ConversationInput
import com.forest.forestchat.ui.create.group.adapter.CreateGroupAdapter
import com.forest.forestchat.ui.create.group.models.CreateGroupInput
import com.forest.forestchat.ui.create.group.models.GroupConversationEvent
import com.forest.forestchat.ui.create.group.models.GroupConversationType

class CreateGroupNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding: NavigationCreateGroupBinding

    lateinit var onCreate: () -> Unit
    lateinit var onChangeType: (GroupConversationType) -> Unit
    lateinit var onNameChange: (String) -> Unit

    private val adapter = CreateGroupAdapter()

    init {
        val layoutInflater = LayoutInflater.from(context)
        binding = NavigationCreateGroupBinding.inflate(layoutInflater, this)

        with(binding) {
            back.setOnClickListener { findNavController().popBackStack() }
            create.setOnClickListener { onCreate() }
            typeCardGroup.setOnClickListener { onChangeType(GroupConversationType.Group) }
            checkboxGroup.setOnClickListener { onChangeType(GroupConversationType.Group) }
            typeCardMailing.setOnClickListener { onChangeType(GroupConversationType.MailingList) }
            checkboxMailing.setOnClickListener { onChangeType(GroupConversationType.MailingList) }

            clearGroupName.setOnClickListener {
                groupName.text.clear()
            }
            groupName.doAfterTextChanged { text ->
                onNameChange(text?.toString() ?: "")
                clearGroupName.visibleIf { text?.isNotBlank() ?: false }
            }
            membersRecycler.adapter = adapter
        }

        post {
            val input = getNavigationInput<CreateGroupInput>()
            val size = (input.contacts?.size ?: 0) + (input.newRecipient?.size ?: 0)
            binding.members.text = R.plurals.create_conversation_members.asPlurals(
                context,
                size
            )

            adapter.updateData(input)
        }
    }

    fun updateType(type: GroupConversationType) {
        binding.checkboxGroup.isChecked = type == GroupConversationType.Group
        binding.checkboxMailing.isChecked = type == GroupConversationType.MailingList
    }

    fun onEvent(event: GroupConversationEvent) {
        when (event) {
            is GroupConversationEvent.GoToConversation -> {
                val input = ConversationInput(event.conversation)
                findNavController().navigate(CreateGroupFragmentDirections.goToConversation(input))
            }
        }
    }

}