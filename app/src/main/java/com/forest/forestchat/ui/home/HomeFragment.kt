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
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Telephony
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import com.forest.forestchat.R
import com.forest.forestchat.app.TransversalBusEvent
import com.forest.forestchat.observer.ContactObserver
import com.forest.forestchat.ui.base.fragment.NavigationFragment
import com.forest.forestchat.ui.chats.ChatsViewModel
import com.forest.forestchat.ui.chats.ConversationEvent
import com.forest.forestchat.ui.dashboard.DashboardViewModel
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.zhuinden.liveevent.observe
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HomeFragment : NavigationFragment() {

    private val chatsViewModel: ChatsViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels()

    private val navigationView: HomeNavigationView
        get() = view as HomeNavigationView

    private var homeTab = HomeTab.Chats
    private var consentInformation: ConsentInformation? = null
    private var consentForm: ConsentForm? = null

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    override fun buildNavigationView(): View = HomeNavigationView(requireContext())

    override fun getStatusBarBgColor(): Int = when (homeTab) {
        HomeTab.Chats -> R.color.toolbarBackground
        HomeTab.Dashboard -> R.color.background
    }

    override fun getNavigationBarBgColor(): Int = R.color.background

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(navigationView) {
            requestSmsPermissionChats = { chatsViewModel.getConversations() }
            onSearchChangedChats = chatsViewModel::onSearchChange
            optionSelected = chatsViewModel::conversationOptionSelected
            onConversationSelected = chatsViewModel::onConversationSelected
            onConversationDeleted = chatsViewModel::removeConversation
            toggleTab = {
                homeTab = it
                updateStatusBarMode()
                updateNavigationBar()
            }
        }

        with(chatsViewModel) {
            chatsEvent().observe(viewLifecycleOwner) { event ->
                when (event) {
                    ConversationEvent.RequestDefaultSms -> showDefaultSmsDialog()
                    ConversationEvent.RequestPermission -> requestPermission()
                    is ConversationEvent.AddContact -> addContact(event.address)
                    else -> null
                }
                navigationView.conversationEvent(event)
            }
        }

        checkConsent()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @Suppress("unused")
    fun onMessageEvent(event: TransversalBusEvent) {
        when (event) {
            is TransversalBusEvent.DefaultSmsChangedEvent -> chatsViewModel.onDefaultSmsChange(event)
            else -> null
        }
    }

    private fun showDefaultSmsDialog() {
        activity?.let { fActivity ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = fActivity.getSystemService(RoleManager::class.java) as RoleManager
                resultLauncher.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS))
            } else {
                val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, fActivity.packageName)
                fActivity.startActivity(intent)
            }
        }
    }

    private fun requestPermission() {
        activity?.let {
            ActivityCompat.requestPermissions(
                it, arrayOf(
                    Manifest.permission.READ_SMS,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.READ_CONTACTS
                ), 0
            )
        }
    }

    private fun checkConsent() {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        context?.let { ctx ->
            consentInformation = UserMessagingPlatform.getConsentInformation(context)
            consentInformation?.requestConsentInfoUpdate(
                activity,
                params,
                {
                    // The consent information state was updated.
                    // You are now ready to check if a form is available.
                    if (consentInformation?.isConsentFormAvailable == true) {
                        loadForm(ctx)
                    } else {
                        init(ctx)
                    }
                },
                {
                    // Handle the error.
                    init(ctx)
                })
        }
    }

    private fun loadForm(context: Context) {
        UserMessagingPlatform.loadConsentForm(
            context,
            { consentForm ->
                this@HomeFragment.consentForm = consentForm
                if (consentInformation!!.consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
                    consentForm.show(activity) {
                        // Handle dismissal by reloading form.
                        loadForm(context)
                    }
                } else {
                    init(context)
                }
            }
        ) {
            // Handle the error
            init(context)
        }
    }

    private fun init(context: Context) {
        MobileAds.initialize(context)
        chatsViewModel.getConversations()
    }

    private fun addContact(address: String) {
        val intent = Intent(Intent.ACTION_INSERT)
            .setType(ContactsContract.Contacts.CONTENT_TYPE)
            .putExtra(ContactsContract.Intents.Insert.PHONE, address)

        context?.let { ContactObserver(it) { chatsViewModel.onContactChanged() }.start() }
        activity?.startActivity(intent)
    }

}