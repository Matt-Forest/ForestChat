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
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import com.forest.forestchat.R
import com.forest.forestchat.app.TransversalBusEvent
import com.forest.forestchat.extensions.observe
import com.forest.forestchat.extensions.observeEvents
import com.forest.forestchat.ui.base.fragment.NavigationFragment
import com.forest.forestchat.ui.conversations.HomeConversationsViewModel
import com.forest.forestchat.ui.conversations.models.HomeConversationEvent
import com.forest.forestchat.ui.dashboard.DashboardViewModel
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HomeFragment : NavigationFragment() {

    private val conversationsViewModel: HomeConversationsViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels()

    private val navigationView: HomeNavigationView
        get() = view as HomeNavigationView

    private var homeTab = HomeTab.Conversations
    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    override fun buildNavigationView(): View = HomeNavigationView(requireContext())

    override fun getStatusBarBgColor(): Int = when (homeTab) {
        HomeTab.Conversations -> R.color.toolbarBackground
        HomeTab.Dashboard -> R.color.background
    }

    override fun getNavigationBarBgColor(): Int = R.color.background

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navigationView.toggleTab = {
            homeTab = it
            updateStatusBarMode()
            updateNavigationBar()
        }

        with(navigationView.getConversationsView()) {
            requestSmsPermission = { conversationsViewModel.getConversations() }
            onSearchChange = conversationsViewModel::onSearchChange
            optionSelected = conversationsViewModel::conversationOptionSelected
            onConversationEvent = conversationsViewModel::onConversationEvent
            onConversationDeleted = conversationsViewModel::removeConversation
            onContactChanged = conversationsViewModel::onContactChanged
            bannerIsLoad = conversationsViewModel::bannerIsLoad
        }

        with(conversationsViewModel) {
            observe(isLoading(), navigationView.getConversationsView()::setLoading)
            observe(state(), navigationView.getConversationsView()::updateState)
            observe(bannerVisible(), navigationView.getConversationsView()::updateBannerVisibility)
            observeEvents(eventSource()) { event ->
                when (event) {
                    is HomeConversationEvent.RequestPermission -> requestPermission()
                    is HomeConversationEvent.RequestDefaultSms -> requestDefaultSmsDialog()
                    else -> null
                }
                navigationView.getConversationsView().event(event)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkAdsConsent()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @Suppress("unused")
    fun onTransversalEvent(event: TransversalBusEvent) {
        when (event) {
            is TransversalBusEvent.DefaultSmsChangedEvent ->
                conversationsViewModel.onDefaultSmsChange(event)
            TransversalBusEvent.RefreshMessages -> conversationsViewModel.getConversations()
        }
    }

    private fun checkAdsConsent() {
        val consentInformation = UserMessagingPlatform.getConsentInformation(requireContext())
        consentInformation?.requestConsentInfoUpdate(
            activity,
            ConsentRequestParameters.Builder().build(),
            {
                // The consent information state was updated.
                // You are now ready to check if a form is available.
                if (consentInformation.isConsentFormAvailable) {
                    loadForm(consentInformation)
                } else {
                    init()
                }
            },
            {
                // Handle the error.
                init()
            })
    }

    private fun loadForm(consentInformation: ConsentInformation) {
        UserMessagingPlatform.loadConsentForm(
            context,
            { consentForm ->
                if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
                    consentForm.show(activity) {
                        // Handle dismissal by reloading form.
                        loadForm(consentInformation)
                    }
                } else {
                    init()
                }
            }
        ) {
            // Handle the error
            init()
        }
    }

    private fun init() {
        MobileAds.initialize(requireContext()) {
            conversationsViewModel.getConversations()
            navigationView.getConversationsView().initBanner()
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