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

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Telephony
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.forest.forestchat.R
import com.forest.forestchat.app.TransversalBusEvent
import com.forest.forestchat.extensions.observe
import com.forest.forestchat.ui.NavigationViewModel
import com.forest.forestchat.ui.base.fragment.NavigationFragment
import com.forest.forestchat.ui.conversations.HomeConversationsViewModel
import com.forest.forestchat.ui.conversations.models.HomeConversationEvent
import com.zhuinden.liveevent.observe
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HomeFragment : NavigationFragment() {

    private val conversationsViewModel: HomeConversationsViewModel by viewModels()
    private val navigationViewModel: NavigationViewModel by activityViewModels()

    private val navigationView: HomeNavigationView
        get() = view as HomeNavigationView

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    override fun buildNavigationView(): View = HomeNavigationView(requireContext())

    override fun getStatusBarBgColor(): Int = R.color.toolbarBackground

    override fun getNavigationBarBgColor(): Int = R.color.bottomNavBackground

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(navigationView.getConversationsView()) {
            requestSmsPermission = { conversationsViewModel.getConversations() }
            onSearchChange = conversationsViewModel::onSearchChange
            optionSelected = conversationsViewModel::conversationOptionSelected
            onConversationEvent = conversationsViewModel::onConversationEvent
            onConversationDeleted = conversationsViewModel::removeConversation
            onContactChanged = conversationsViewModel::onContactChanged
            onSearchContactClick = conversationsViewModel::searchContact
            onSearchConversationClick = conversationsViewModel::searchConversation
        }

        with(conversationsViewModel) {
            observe(isLoading(), navigationView.getConversationsView()::setLoading)
            observe(state(), navigationView.getConversationsView()::updateState)
            eventSource().observe(viewLifecycleOwner) { event ->
                when (event) {
                    is HomeConversationEvent.RequestPermission -> requestPermission()
                    is HomeConversationEvent.RequestDefaultSms -> requestDefaultSmsDialog()
                    is HomeConversationEvent.ShowContact -> showContact(event.lookupKey)
                    else -> null
                }
                navigationView.getConversationsView().event(event)
            }
        }

        with(navigationViewModel) {
            deeplinkEventSource().observe(viewLifecycleOwner, navigationView::deeplinkEvent)
        }

        navigationViewModel.consumeRedirection()
    }

    override fun onResume() {
        super.onResume()
        conversationsViewModel.getConversations()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @Suppress("unused")
    override fun onTransversalEvent(event: TransversalBusEvent) {
        when (event) {
            is TransversalBusEvent.DefaultSmsChangedEvent ->
                conversationsViewModel.onDefaultSmsChange(event)
            TransversalBusEvent.RefreshMessages -> conversationsViewModel.getConversations()
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

    private fun showContact(lookupKey: String) {
        val intent = Intent(Intent.ACTION_VIEW)
            .setData(
                Uri.withAppendedPath(
                    ContactsContract.Contacts.CONTENT_LOOKUP_URI,
                    lookupKey
                )
            )

        startActivityExternal(intent)
    }

    private fun startActivityExternal(intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            resultLauncher.launch(intent)
        } else {
            requireActivity().startActivity(intent)
        }
    }

}