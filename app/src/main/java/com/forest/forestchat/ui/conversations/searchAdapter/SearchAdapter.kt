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
package com.forest.forestchat.ui.conversations.searchAdapter

import android.content.Context
import android.view.ViewGroup
import com.forest.forestchat.R
import com.forest.forestchat.domain.models.SearchConversationResult
import com.forest.forestchat.domain.models.contact.Contact
import com.forest.forestchat.extensions.asPlurals
import com.forest.forestchat.extensions.asString
import com.forest.forestchat.extensions.getConversationTimestamp
import com.forest.forestchat.ui.base.recycler.BaseAdapter
import com.forest.forestchat.ui.base.recycler.BaseItem
import com.forest.forestchat.ui.base.recycler.BaseHolder
import com.forest.forestchat.ui.conversations.searchAdapter.contact.SearchContactHolder
import com.forest.forestchat.ui.conversations.searchAdapter.contact.SearchContactItem
import com.forest.forestchat.ui.conversations.searchAdapter.conversation.SearchConversationHolder
import com.forest.forestchat.ui.conversations.searchAdapter.conversation.SearchConversationItem
import com.forest.forestchat.ui.conversations.searchAdapter.header.SearchHeaderHolder
import com.forest.forestchat.ui.conversations.searchAdapter.header.SearchHeaderItem
import com.forest.forestchat.ui.common.mappers.buildAvatar
import com.forest.forestchat.ui.common.mappers.buildSingleAvatar

class SearchAdapter : BaseAdapter() {

    override fun buildViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<*>? =
        when (viewType) {
            SearchViewTypes.HEADER -> SearchHeaderHolder(parent)
            SearchViewTypes.CONVERSATION -> SearchConversationHolder(parent)
            SearchViewTypes.CONTACT -> SearchContactHolder(parent)
            else -> null
        }

    fun setData(
        context: Context,
        conversations: List<SearchConversationResult>,
        contacts: List<Contact>
    ) {
        val items : MutableList<BaseItem> = mutableListOf()

        if (conversations.isNotEmpty()) {
            items.add(SearchHeaderItem(R.string.conversations_search_conversation.asString(context)))
        }

        conversations.forEach { conversation ->
            items.add(buildConversation(context, conversation))
        }

        if (contacts.isNotEmpty()) {
            items.add(SearchHeaderItem(R.string.conversations_search_contact.asString(context)))
        }

        contacts.forEach { contact ->
            items.add(buildContact(contact))
        }

        submitList(items)
    }

    private fun buildContact(contact: Contact): SearchContactItem =
        SearchContactItem(
            id = contact.id,
            name = contact.name ?: "",
            number = (contact.getDefaultNumber() ?: contact.numbers.first()).address,
            avatarType = buildSingleAvatar(contact, false),
        )

    private fun buildConversation(
        context: Context,
        conversationsResult: SearchConversationResult
    ): SearchConversationItem =
        SearchConversationItem(
            id = conversationsResult.conversation.id,
            title = conversationsResult.conversation.getTitle(),
            message = R.plurals.conversations_search_conversation_count.asPlurals(
                context,
                conversationsResult.messages
            ),
            date = conversationsResult.conversation.lastMessage?.date?.takeIf { it > 0 }
                ?.getConversationTimestamp(context) ?: "",
            avatarType = buildAvatar(conversationsResult.conversation.recipients),
        )

}