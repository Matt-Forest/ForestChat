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
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.forest.forestchat.domain.models.message.Message
import kotlinx.android.parcel.Parcelize

@Entity
@Parcelize
data class Conversation(
    @PrimaryKey
    val id: Long,
    val archived: Boolean,
    val blocked: Boolean,
    val pinned: Boolean,
    val grouped: Boolean,
    val recipients: List<Recipient>,
    val lastMessage: Message?,
    val draft: String?,
    val name: String?
) : Parcelable {

    fun getTitle(): String {
        return name.takeIf { it?.isNotBlank() == true }
            ?: recipients.joinToString { recipient -> recipient.getDisplayName() }
    }

}