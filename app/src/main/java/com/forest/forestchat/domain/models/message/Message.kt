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
package com.forest.forestchat.domain.models.message

import android.content.ContentUris
import android.net.Uri
import android.os.Parcelable
import android.provider.Telephony
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.forest.forestchat.domain.models.message.mms.MessageMms
import com.forest.forestchat.domain.models.message.sms.MessageSms
import kotlinx.android.parcel.Parcelize

@Entity
@Parcelize
data class Message(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val threadId: Long,
    val contentId: Long,
    val address: String?,
    val box: MessageBox,
    val type: MessageType,
    val date: Long,
    val dateSent: Long,
    val read: Boolean,
    val seen: Boolean,
    val subId: Int?,
    val sms: MessageSms?,
    val mms: MessageMms?
) : Parcelable {

    fun isUser(): Boolean {
        val isIncomingMms =
            type == MessageType.Mms && (box == MessageBox.Inbox || box == MessageBox.All)
        val isIncomingSms =
            type == MessageType.Sms && (box == MessageBox.Inbox || box == MessageBox.All)

        return !(isIncomingMms || isIncomingSms)
    }

    /**
     * Returns the text that should be displayed when a preview of the message
     * needs to be displayed, such as in the conversation view or in a notification
     */
    fun getSummary(): String = when (type) {
        MessageType.Sms -> sms?.body
        else -> mms?.getSummary()
    } ?: ""

    fun getText(): String = when (type) {
        MessageType.Sms -> sms?.body
        else -> mms?.getText()
    } ?: ""

    fun getUri(): Uri? {
        val baseUri = when (type) {
            MessageType.Sms -> Telephony.Sms.CONTENT_URI
            MessageType.Mms -> Telephony.Mms.CONTENT_URI
            MessageType.Unknown -> return null
        }
        return ContentUris.withAppendedId(baseUri, contentId)
    }

    fun isFailed(): Boolean =
        when (type) {
            MessageType.Sms -> box == MessageBox.Failed
            MessageType.Mms -> (mms != null && mms.errorCode >= Telephony.MmsSms.ERR_TYPE_GENERIC_PERMANENT)
                    || box == MessageBox.Failed
            else -> false
        }

    fun compareSender(other: Message): Boolean = when {
        isUser() && other.isUser() -> subId == other.subId
        !isUser() && !other.isUser() -> subId == other.subId && address == other.address
        else -> false
    }

}
