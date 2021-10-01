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
package com.forest.forestchat.domain.mappers

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import com.forest.forestchat.domain.models.contact.Contact
import com.forest.forestchat.domain.models.contact.PhoneNumber
import com.forest.forestchat.extensions.getIntValue
import com.forest.forestchat.extensions.getLongValue
import com.forest.forestchat.extensions.getStringValue

fun Cursor.toContact(context: Context) = Contact(
    lookupKey = getStringValue(ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY),
    numbers = mutableListOf(
        PhoneNumber(
            id = getLongValue(ContactsContract.CommonDataKinds.Phone._ID),
            address = getStringValue(ContactsContract.CommonDataKinds.Phone.NUMBER),
            type = ContactsContract.CommonDataKinds.Phone.getTypeLabel(
                context.resources,
                getIntValue(ContactsContract.CommonDataKinds.Phone.TYPE),
                getStringValue(ContactsContract.CommonDataKinds.Phone.LABEL)
            ).toString(),
            isDefault = false
        )
    ),
    name = getStringValue(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME),
    photoUri = getStringValue(ContactsContract.CommonDataKinds.Phone.PHOTO_URI),
    starred = getIntValue(ContactsContract.CommonDataKinds.Phone.STARRED) != 0,
    lastUpdate = getLongValue(ContactsContract.CommonDataKinds.Phone.CONTACT_LAST_UPDATED_TIMESTAMP),
)