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
package com.forest.forestchat.ui.splash

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.forest.forestchat.R
import com.forest.forestchat.ui.base.fragment.NavigationFragment
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

class SplashFragment : NavigationFragment() {

    override fun buildNavigationView(): View = SplashNavigationView(requireContext())

    override fun getStatusBarBgColor(): Int = R.color.toolbarBackground

    override fun getNavigationBarBgColor(): Int = R.color.background

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        checkAdsConsent()
    }

    private fun checkAdsConsent() {
        val consentInformation = UserMessagingPlatform.getConsentInformation(context)
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
            findNavController().navigate(SplashFragmentDirections.goToHome())
        }
    }

}