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
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doAfterTextChanged
import com.forest.forestchat.R
import com.forest.forestchat.databinding.NavigationConversationsBinding
import com.forest.forestchat.extensions.asString
import com.forest.forestchat.extensions.visible
import com.forest.forestchat.extensions.visibleIf
import com.forest.forestchat.ui.conversations.adapter.ConversationsAdapter
import com.forest.forestchat.ui.conversations.dialog.ConversationDeleteDialog
import com.forest.forestchat.ui.conversations.dialog.ConversationOptionType
import com.forest.forestchat.ui.conversations.dialog.ConversationOptionsDialog
import com.forest.forestchat.ui.conversations.searchAdapter.SearchAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError

class ConversationsNavigationView : ConstraintLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    lateinit var requestSmsPermission: () -> Unit
    lateinit var onSearchChange: (String) -> Unit
    lateinit var optionSelected: (ConversationOptionType) -> Unit
    lateinit var onConversationSelected: (Long) -> Unit
    lateinit var onConversationDeleted: (Long) -> Unit

    private val binding: NavigationConversationsBinding
    private var conversationsAdapter = ConversationsAdapter { onConversationSelected(it) }
    private val searchAdapter = SearchAdapter()
    private var bannerLoaded: Boolean = false

    init {
        val layoutInflater = LayoutInflater.from(context)
        binding = NavigationConversationsBinding.inflate(layoutInflater, this)

        with(binding) {
            changePermission.setOnClickListener { requestSmsPermission() }
            userProfile.setOnClickListener {
                when (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO) {
                    true -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    false -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }

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

    private fun initBanner() {
        val adRequest = AdRequest.Builder().build()
        binding.adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                bannerLoaded = true
                binding.adView.visible()
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                // Code to be executed when an ad request fails.
                bannerLoaded = false
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

    fun event(event: ConversationEvent) {
        with(binding) {
            if (
                event !is ConversationEvent.ShowConversationOptions
                && event !is ConversationEvent.RequestDeleteDialog
                && event !is ConversationEvent.AddContact
            ) {
                empty.visibleIf { event is ConversationEvent.NoConversationsData || event is ConversationEvent.NoSearchData }
                recyclerChat.visibleIf { event is ConversationEvent.ConversationsData || event is ConversationEvent.SearchData }
                loadingData.visibleIf { event is ConversationEvent.Loading }
                requestPermission.visibleIf { event is ConversationEvent.RequestPermission }
                adView.visibleIf { event is ConversationEvent.ConversationsData && bannerLoaded }
            }

            when (event) {
                ConversationEvent.NoConversationsData -> {
                    empty.text = R.string.conversations_empty_conversation.asString(context)
                }
                ConversationEvent.NoSearchData -> {
                    empty.text = R.string.conversations_empty_search.asString(context)
                }
                is ConversationEvent.ConversationsData -> {
                    if (recyclerChat.adapter !== conversationsAdapter) {
                        recyclerChat.adapter = conversationsAdapter
                    }
                    conversationsAdapter.apply {
                        setConversations(context, event.conversations, event.adsActivated)
                    }
                }
                is ConversationEvent.SearchData -> {
                    if (recyclerChat.adapter !== searchAdapter) {
                        recyclerChat.adapter = searchAdapter
                    }
                    searchAdapter.apply {
                        setData(context, event.conversations, event.contacts)
                    }
                }
                is ConversationEvent.ShowConversationOptions -> {
                    ConversationOptionsDialog(
                        context,
                        { optionSelected(it) },
                        event.showAddToContacts,
                        event.showPin,
                        event.showPinnedOff,
                        event.showMarkAsRead
                    ).create().show()
                }
                is ConversationEvent.RequestDeleteDialog -> {
                    ConversationDeleteDialog(context) { onConversationDeleted(event.id) }
                        .create()
                        .show()
                }
                ConversationEvent.AdsConsentComplete -> {
                    initBanner()
                }
                else -> null
            }
        }
    }

}