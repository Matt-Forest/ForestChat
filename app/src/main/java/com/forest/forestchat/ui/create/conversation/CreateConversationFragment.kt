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

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.forest.forestchat.R
import com.forest.forestchat.extensions.observe
import com.forest.forestchat.extensions.observeEvents
import com.forest.forestchat.ui.base.fragment.NavigationFragment

class CreateConversationFragment : NavigationFragment() {

    private val viewModel: CreateConversationViewModel by viewModels()

    private val navigationView: CreateConversationNavigationView
        get() = view as CreateConversationNavigationView

    override fun buildNavigationView(): View = CreateConversationNavigationView(requireContext())

    override fun getStatusBarBgColor(): Int = R.color.background

    override fun getNavigationBarBgColor(): Int = R.color.background

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(navigationView) {
            onContactCheck = viewModel::contactCheck
            onSearchChange = viewModel::onSearchChange
            onCreateOrNext = viewModel::create
            onRemoveSelected = viewModel::removeRecipientsSelect
            onAddNewRecipient = viewModel::addNewRecipient
        }

        with(viewModel) {
            observe(contactsSearch(), navigationView::updateContactsSearch)
            observe(contactsSelected(), navigationView::updateSelectedRecipient)
            observe(newRecipient(), navigationView::updateNewRecipient)
            observe(buttonState(), navigationView::updateFabButton)
            observeEvents(eventSource(), navigationView::onEvent)
        }
    }

}