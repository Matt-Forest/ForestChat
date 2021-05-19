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

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.forest.forestchat.domain.models.message.mms.MessageMms
import com.forest.forestchat.domain.models.message.sms.MessageSms

@Entity
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
    val seen: Boolean,
    val read: Boolean,
    val locked: Boolean,
    val subId: Int?,
    val sms: MessageSms?,
    val mms: MessageMms?
) {

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

}
