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
package com.forest.forestchat.ui.create.conversation

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.findNavController
import com.forest.forestchat.R
import com.forest.forestchat.databinding.NavigationCreateConversationBinding
import com.forest.forestchat.extensions.*
import com.forest.forestchat.ui.common.avatar.AvatarType
import com.forest.forestchat.ui.create.conversation.adapter.search.CreateConversationSearchAdapter
import com.forest.forestchat.ui.create.conversation.adapter.selected.CreateConversationSelectedAdapter
import com.forest.forestchat.ui.create.conversation.models.ContactSearch
import com.forest.forestchat.ui.create.conversation.models.ContactSelected
import com.forest.forestchat.ui.create.conversation.models.CreateConversationButtonState

class CreateConversationNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding: NavigationCreateConversationBinding

    lateinit var onContactCheck: (Long) -> Unit
    lateinit var onRemoveSelected: (Int) -> Unit
    lateinit var onSearchChange: (String) -> Unit
    lateinit var onCreateOrNext: () -> Unit
    lateinit var onAddNewRecipient: () -> Unit

    private val adapter = CreateConversationSearchAdapter { onContactCheck(it) }
    private val adapterSelected = CreateConversationSelectedAdapter { onRemoveSelected(it) }

    init {
        val layoutInflater = LayoutInflater.from(context)
        binding = NavigationCreateConversationBinding.inflate(layoutInflater, this)

        with(binding) {
            back.setOnClickListener { findNavController().popBackStack() }
            create.setOnClickListener { onCreateOrNext() }
            newRecipient.setOnClickListener { onAddNewRecipient() }
            newAvatar.updateAvatars(AvatarType.Single.Profile)

            members.text = R.plurals.create_conversation_members.asPlurals(
                context,
                0
            )
            clearSearch.setOnClickListener {
                searchContact.text.clear()
            }
            searchContact.doAfterTextChanged { text ->
                onSearchChange(text?.toString() ?: "")
                clearSearch.visibleIf { text?.isNotBlank() ?: false }
            }

            contactRecycler.adapter = adapter
            selectedContactRecycler.adapter = adapterSelected
        }
    }

    fun updateContactsSearch(contactSearch: List<ContactSearch>) {
        adapter.updateData(contactSearch)
    }

    fun updateSelectedRecipient(selectedRecipient: List<ContactSelected>) {
        adapterSelected.update(selectedRecipient)
        binding.selectedContactRecycler.visibleIf { selectedRecipient.isNotEmpty() }
        binding.members.text = R.plurals.create_conversation_members.asPlurals(
            context,
            selectedRecipient.size
        )
    }

    fun updateNewRecipient(address: String?) {
        with(binding) {
            newRecipient.visibleIf { !address.isNullOrBlank() }
            newNumberPhone.text = address
        }
    }

    fun updateFabButton(state: CreateConversationButtonState) {
        with(binding) {
            when (state) {
                CreateConversationButtonState.None -> create.gone()
                CreateConversationButtonState.Next -> create.apply {
                    text = R.string.create_conversation_button_next.asString(context)
                    visible()
                }
                CreateConversationButtonState.Create -> create.apply {
                    text = R.string.create_conversation_button_create.asString(context)
                    visible()
                }
            }
        }
    }

}