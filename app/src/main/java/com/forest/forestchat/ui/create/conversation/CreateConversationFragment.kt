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

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import com.forest.forestchat.R
import com.forest.forestchat.extensions.observe
import com.forest.forestchat.ui.base.fragment.NavigationFragment
import com.forest.forestchat.ui.create.conversation.models.CreateConversationEvent
import com.zhuinden.liveevent.observe

class CreateConversationFragment : NavigationFragment() {

    private val viewModel: CreateConversationViewModel by viewModels()

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    private val navigationView: CreateConversationNavigationView
        get() = view as CreateConversationNavigationView

    override fun buildNavigationView(): View = CreateConversationNavigationView(requireContext())

    override fun getStatusBarBgColor(): Int = R.color.toolbarBackground

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
            eventSource().observe(viewLifecycleOwner) { event ->
                when (event) {
                    CreateConversationEvent.RequestDefaultSms -> requestDefaultSmsDialog()
                    CreateConversationEvent.RequestPermission -> requestPermission()
                    else -> navigationView.onEvent(event)
                }
            }
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(), arrayOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_CONTACTS
            ), 0
        )
    }

    private fun requestDefaultSmsDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager =
                requireActivity().getSystemService(RoleManager::class.java) as RoleManager
            resultLauncher.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS))
        } else {
            val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, requireActivity().packageName)
            requireActivity().startActivity(intent)
        }
    }

}