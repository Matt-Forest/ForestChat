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

import android.database.Cursor
import android.telephony.PhoneNumberUtils
import com.forest.forestchat.domain.models.Recipient
import com.forest.forestchat.domain.models.contact.Contact

fun Cursor.toRecipient(contacts: List<Contact>?) : Recipient {
    val address = getString(1)
    return Recipient(
        id = getLong(0),
        address = address,
        contact = contacts?.firstOrNull { contact ->
            contact.numbers.any { PhoneNumberUtils.compare(address, it.address) }
        }
    )
}