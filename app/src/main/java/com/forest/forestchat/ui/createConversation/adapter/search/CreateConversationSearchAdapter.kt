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
package com.forest.forestchat.ui.createConversation.adapter.search

import android.view.ViewGroup
import com.forest.forestchat.domain.models.contact.Contact
import com.forest.forestchat.extensions.removeAccents
import com.forest.forestchat.ui.base.recycler.BaseAdapter
import com.forest.forestchat.ui.base.recycler.BaseHolder
import com.forest.forestchat.ui.base.recycler.BaseItem
import com.forest.forestchat.ui.common.mappers.buildSingleAvatar
import com.forest.forestchat.ui.createConversation.models.ContactSearch


class CreateConversationSearchAdapter(
    private val onCheck: (Long) -> Unit
) : BaseAdapter() {

    override fun buildViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<*> =
        CreateConversationSearchContactHolder(parent, onCheck)

    override fun onPayload(holder: BaseHolder<BaseItem>, position: Int, payload: Any) {
        payload as CreateConversationSearchPayload

        when (val holder = holder as BaseHolder<*>) {
            is CreateConversationSearchContactHolder -> holder.onPayload(payload)
        }
    }

    fun updateData(contacts: List<ContactSearch>) {
        val contactSorted = contacts.sortedBy {
            buildName(it.contact).removeAccents().lowercase()
        }
        val items = contactSorted.mapIndexed { index, contactSearchSelect ->
            val contact = contactSearchSelect.contact
            val contactName = buildName(contact)
            val previousContactNameLetter = when (index > 0) {
                true -> buildName(contactSorted[index - 1].contact).removeAccents()[0]
                false -> null
            }

            CreateConversationSearchContactItem(
                contactId = contact.id,
                avatarType = buildSingleAvatar(contact, false),
                name = contactName,
                number = when (contact.name != null) {
                    true -> buildAddress(contact)
                    false -> null
                },
                letter = when (!contactSearchSelect.isCheck && (previousContactNameLetter == null ||
                        !previousContactNameLetter.equals(contactName.removeAccents()[0], true))
                ) {
                    true -> contactName[0]
                    false -> null
                },
                isChecked = contactSearchSelect.isCheck
            )
        }

        submitList(items.toList())
    }

    private fun buildName(contact: Contact): String =
        contact.name ?: buildAddress(contact)

    private fun buildAddress(contact: Contact): String =
        (contact.getDefaultNumber() ?: contact.numbers.first()).address

}