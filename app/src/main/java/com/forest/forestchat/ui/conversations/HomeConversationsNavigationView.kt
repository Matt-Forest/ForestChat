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
package com.forest.forestchat.ui.conversations

import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.findNavController
import com.forest.forestchat.databinding.NavigationConversationsBinding
import com.forest.forestchat.extensions.asString
import com.forest.forestchat.extensions.goneIf
import com.forest.forestchat.extensions.visibleIf
import com.forest.forestchat.observer.ContactObserver
import com.forest.forestchat.ui.common.dialog.ConversationDeleteDialog
import com.forest.forestchat.ui.conversation.models.ConversationInput
import com.forest.forestchat.ui.common.conversations.adapter.ConversationsAdapter
import com.forest.forestchat.ui.common.conversations.adapter.conversation.ConversationItemEvent
import com.forest.forestchat.ui.common.conversations.dialog.ConversationOptionType
import com.forest.forestchat.ui.common.conversations.dialog.ConversationOptionsDialog
import com.forest.forestchat.ui.conversations.models.HomeConversationEvent
import com.forest.forestchat.ui.conversations.models.HomeConversationsState
import com.forest.forestchat.ui.conversations.searchAdapter.SearchAdapter
import com.forest.forestchat.ui.home.HomeFragmentDirections
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError

class HomeConversationsNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    lateinit var requestSmsPermission: () -> Unit
    lateinit var onContactChanged: () -> Unit
    lateinit var onSearchChange: (String) -> Unit
    lateinit var optionSelected: (ConversationOptionType) -> Unit
    lateinit var onConversationEvent: (ConversationItemEvent) -> Unit
    lateinit var onConversationDeleted: (Long) -> Unit
    lateinit var bannerIsLoad: (Boolean) -> Unit
    lateinit var onSearchConversationClick: (Long) -> Unit
    lateinit var onSearchContactClick: (Long) -> Unit

    private val binding: NavigationConversationsBinding
    private var conversationsAdapter = ConversationsAdapter { onConversationEvent(it) }
    private val searchAdapter = SearchAdapter(
        { onSearchConversationClick(it) },
        { onSearchContactClick(it) }
    )


    init {
        val layoutInflater = LayoutInflater.from(context)
        binding = NavigationConversationsBinding.inflate(layoutInflater, this)

        with(binding) {
            changePermission.setOnClickListener { requestSmsPermission() }
            userProfile.setOnClickListener { findNavController().navigate(HomeFragmentDirections.goToSettings()) }

            clearSearch.setOnClickListener {
                searchChat.text.clear()
            }
            searchChat.doAfterTextChanged {
                it?.let { text ->
                    onSearchChange(text.toString())
                    clearSearch.visibleIf { text.isNotBlank() }
                }
            }
        }
    }

    fun initBanner() {
        val adRequest = AdRequest.Builder().build()
        binding.adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                bannerIsLoad(true)
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                // Code to be executed when an ad request fails.
                bannerIsLoad(false)
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        }
        binding.adView.loadAd(adRequest)
    }

    fun event(event: HomeConversationEvent) {
        when (event) {
            is HomeConversationEvent.GoToConversation -> {
                val input = ConversationInput(event.conversation)
                findNavController().navigate(HomeFragmentDirections.goToConversation(input))
            }
            is HomeConversationEvent.ShowConversationOptions -> {
                ConversationOptionsDialog(
                    context,
                    { optionSelected(it) },
                    event.showAddToContacts,
                    event.showPin,
                    event.showPinnedOff,
                    event.showMarkAsRead,
                    false
                ).create().show()
            }
            is HomeConversationEvent.RequestDeleteDialog -> {
                ConversationDeleteDialog(context) { onConversationDeleted(event.id) }
                    .create()
                    .show()
            }
            is HomeConversationEvent.AddContact -> {
                val intent = Intent(Intent.ACTION_INSERT)
                    .setType(ContactsContract.Contacts.CONTENT_TYPE)
                    .putExtra(ContactsContract.Intents.Insert.PHONE, event.address)

                context?.let {
                    ContactObserver(it) { onContactChanged() }.start()
                    it.startActivity(intent)
                }
            }
            else -> null
        }
    }

    fun updateState(state: HomeConversationsState) {
        with(binding) {
            empty.visibleIf { state is HomeConversationsState.Empty }
            recyclerChat.goneIf { state is HomeConversationsState.Empty }
            requestPermission.visibleIf { state is HomeConversationsState.RequestPermission }

            when (state) {
                is HomeConversationsState.Empty -> empty.text = state.label.asString(context)
                is HomeConversationsState.Conversations -> {
                    if (recyclerChat.adapter !== conversationsAdapter) {
                        recyclerChat.adapter = conversationsAdapter
                    }
                    conversationsAdapter.apply {
                        setConversations(context, state.conversations, false)
                    }
                }
                is HomeConversationsState.Search -> {
                    if (recyclerChat.adapter !== searchAdapter) {
                        recyclerChat.adapter = searchAdapter
                    }
                    searchAdapter.apply {
                        setData(context, state.conversations, state.contacts)
                    }
                }
                else -> null
            }
        }
    }

    fun setLoading(isVisible: Boolean) {
        with(binding.loadBar) {
            when (isVisible) {
                true -> startLoading()
                false -> stopLoading()
            }
        }
    }

    fun updateBannerVisibility(isVisible: Boolean) {
        binding.adView.visibleIf { isVisible }
    }

}