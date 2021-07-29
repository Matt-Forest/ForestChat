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
package com.forest.forestchat.ui.recipients

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.forest.forestchat.R
import com.forest.forestchat.app.TransversalBusEvent
import com.forest.forestchat.extensions.observe
import com.forest.forestchat.ui.base.fragment.NavigationFragment
import com.forest.forestchat.ui.recipients.models.RecipientsEvent
import com.zhuinden.liveevent.observe
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class RecipientsFragment : NavigationFragment() {

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    private val viewModel: RecipientsViewModel by viewModels()

    private val navigationView: RecipientsNavigationView
        get() = view as RecipientsNavigationView

    override fun buildNavigationView(): View = RecipientsNavigationView(requireContext())

    override fun getStatusBarBgColor(): Int = R.color.background

    override fun getNavigationBarBgColor(): Int = R.color.background

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(navigationView) {
            onRecipientSelected = viewModel::recipientClick
        }

        with(viewModel) {
            observe(recipientsData(), navigationView::setupRecipients)
            eventSource().observe(viewLifecycleOwner) { event ->
                when (event) {
                    is RecipientsEvent.ShowContact -> showContact(
                        event.lookupKey,
                        event.address
                    )
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @Suppress("unused")
    fun onTransversalEvent(event: TransversalBusEvent) {
        // nothing
    }

    private fun showContact(lookupKey: String?, address: String?) {
        if (!lookupKey.isNullOrEmpty()) {
            val intent = Intent(Intent.ACTION_VIEW)
                .setData(
                    Uri.withAppendedPath(
                        ContactsContract.Contacts.CONTENT_LOOKUP_URI,
                        lookupKey
                    )
                )

            startActivityExternal(intent)
        } else if (!address.isNullOrEmpty()) {
            val intent = Intent(Intent.ACTION_INSERT)
                .setType(ContactsContract.Contacts.CONTENT_TYPE)
                .putExtra(ContactsContract.Intents.Insert.PHONE, address)

            startActivityExternal(intent)
        }
    }

    private fun startActivityExternal(intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            resultLauncher.launch(intent)
        } else {
            requireActivity().startActivity(intent)
        }
    }

}