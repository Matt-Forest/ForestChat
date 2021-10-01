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
package com.forest.forestchat.ui.settings.app

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
import com.forest.forestchat.ui.settings.app.models.SettingsAppEvent
import com.forest.forestchat.ui.splash.models.SplashEvent
import com.google.android.ump.ConsentInformation
import com.google.android.ump.UserMessagingPlatform
import com.zhuinden.liveevent.observe

class SettingsAppFragment : NavigationFragment() {

    private val viewModel: SettingsAppViewModel by viewModels()

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    private val navigationView: SettingsAppNavigationView
        get() = view as SettingsAppNavigationView

    override fun buildNavigationView(): View = SettingsAppNavigationView(requireContext())

    override fun getStatusBarBgColor(): Int = R.color.toolbarBackground

    override fun getNavigationBarBgColor(): Int = R.color.background

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(navigationView) {
            onNotifications = { showNotifications() }
            onSync = viewModel::syncData
            onCookiesUpdate = { updateCookies() }
        }

        with(viewModel) {
            observe(loading(), navigationView::updateLoading)
            eventSource().observe(viewLifecycleOwner) { event ->
                when (event) {
                    SettingsAppEvent.RequestPermission -> requestPermission()
                    SettingsAppEvent.RequestDefaultSms -> requestDefaultSmsDialog()
                }
            }
        }
    }

    private fun showNotifications() {
        val intent = Intent()
        intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
        intent.putExtra("android.provider.extra.APP_PACKAGE", context?.packageName)
        requireActivity().startActivity(intent)
    }

    private fun updateCookies() {
        UserMessagingPlatform.loadConsentForm(
            requireContext(),
            { consentForm ->
                consentForm.show(requireActivity()) { }
            }
        ) {
            // Handle the error
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