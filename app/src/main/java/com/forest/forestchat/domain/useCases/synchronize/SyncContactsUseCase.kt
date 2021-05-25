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
package com.forest.forestchat.domain.useCases.synchronize

import android.content.Context
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import com.forest.forestchat.domain.mappers.toContact
import com.forest.forestchat.domain.models.contact.Contact
import com.forest.forestchat.domain.models.contact.PhoneNumber
import com.forest.forestchat.localStorage.database.daos.ContactDao
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SyncContactsUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contactDao: ContactDao
) {

    suspend operator fun invoke() {
        val defaultNumberIds: List<Long>? = contactDao.getAll()
            ?.flatMap { it.numbers }
            ?.filter { it.isDefault }
            ?.map { it.id }
        contactDao.deleteAll()

        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.TYPE,
                ContactsContract.CommonDataKinds.Phone.LABEL,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
                ContactsContract.CommonDataKinds.Phone.STARRED,
                ContactsContract.CommonDataKinds.Phone.CONTACT_LAST_UPDATED_TIMESTAMP
            ),
            null,
            null,
            null
        )?.use { cursor ->
            val contacts = mutableListOf<Contact>()
            while (cursor.moveToNext()) {
                contacts.add(cursor.toContact(context))
            }

            // Remove the same contacts but we keep the different phone numbers and we set the "default" one.
            contacts.groupBy { it.lookupKey }.map { contactsMap ->
                val uniqueNumbers = mutableListOf<PhoneNumber>()
                contactsMap.value
                    .flatMap { it.numbers }
                    .forEach { number ->
                        number.isDefault = defaultNumberIds?.any { id -> id == number.id } == true
                        val duplicate = uniqueNumbers.find { other ->
                            PhoneNumberUtils.compare(number.address, other.address)
                        }

                        if (duplicate == null) {
                            uniqueNumbers += number
                        } else if (!duplicate.isDefault && number.isDefault) {
                            duplicate.isDefault = true
                        }
                    }

                contactDao.insert(contactsMap.value.first().copy(numbers = uniqueNumbers))
            }
        }
    }

}