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
package com.forest.forestchat.domain.models

import android.os.Parcelable
import android.telephony.PhoneNumberUtils
import com.forest.forestchat.domain.models.contact.Contact
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Recipient(
    val id: Long,
    val address: String,
    val contact: Contact?
) : Parcelable {

    fun getDisplayName(): String = contact?.name?.takeIf { it.isNotBlank() }
        ?: PhoneNumberUtils.formatNumber(address, Locale.getDefault().country)
        ?: address

    fun getNumberPhone() : String? = PhoneNumberUtils.formatNumber(address, Locale.getDefault().country)

}