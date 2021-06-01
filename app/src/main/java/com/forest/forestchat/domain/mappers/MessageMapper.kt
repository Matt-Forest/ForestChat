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
import android.provider.Telephony
import com.forest.forestchat.domain.models.message.Message
import com.forest.forestchat.domain.models.message.MessageBox
import com.forest.forestchat.domain.models.message.MessageType
import com.forest.forestchat.domain.models.message.mms.MmsPart

fun Cursor.toMessage(mmsParts: (Long) -> List<MmsPart>, mmsAddress: (Long) -> String?): Message {
    val type: MessageType = when {
        getColumnIndex(Telephony.MmsSms.TYPE_DISCRIMINATOR_COLUMN) != -1 -> getString(
            getColumnIndex(Telephony.MmsSms.TYPE_DISCRIMINATOR_COLUMN)
        )
        getColumnIndex(Telephony.Mms.SUBJECT) != -1 -> "mms"
        getColumnIndex(Telephony.Sms.ADDRESS) != -1 -> "sms"
        else -> null
    }.toMessageType()
    val contentId = getLong(getColumnIndex(Telephony.MmsSms._ID))

    return Message(
        threadId = getLong(getColumnIndex(Telephony.Mms.THREAD_ID)),
        contentId = contentId,
        address = when (type) {
            MessageType.Sms -> getString(getColumnIndex(Telephony.Sms.ADDRESS))
            MessageType.Mms -> mmsAddress(contentId)
            else -> null
        },
        box = MessageBox.values().find {
            it.code == when (type) {
                MessageType.Sms -> getInt(getColumnIndex(Telephony.Sms.TYPE))
                MessageType.Mms -> getInt(getColumnIndex(Telephony.Mms.MESSAGE_BOX))
                else -> 0
            }
        } ?: MessageBox.All,
        type = type,
        date = getLong(getColumnIndex(Telephony.Mms.DATE)).toDate(type),
        dateSent = getLong(getColumnIndex(Telephony.Mms.DATE_SENT)).toDate(type),
        read = getInt(getColumnIndex(Telephony.Mms.READ)) != 0,
        seen = getInt(getColumnIndex(Telephony.Mms.SEEN)) != 0,
        locked = when (type) {
            MessageType.Sms -> getInt(getColumnIndex(Telephony.Sms.LOCKED)) != 0
            MessageType.Mms -> getInt(getColumnIndex(Telephony.Mms.LOCKED)) != 0
            else -> false
        },
        subId = when (type) {
            MessageType.Sms -> when (getColumnIndex(Telephony.Sms.SUBSCRIPTION_ID) != -1) {
                true -> getInt(getColumnIndex(Telephony.Sms.SUBSCRIPTION_ID))
                false -> null
            }
            MessageType.Mms -> when (getColumnIndex(Telephony.Mms.SUBSCRIPTION_ID) != -1) {
                true -> getInt(getColumnIndex(Telephony.Mms.SUBSCRIPTION_ID))
                false -> null
            }
            else -> null
        },
        sms = when (type) {
            MessageType.Sms -> toSms()
            else -> null
        },
        mms = when (type) {
            MessageType.Mms -> toMms(mmsParts(contentId))
            else -> null
        }
    )
}

private fun Long.toDate(type: MessageType): Long = when (type) {
    MessageType.Mms -> this * 1_000
    else -> this
}

private fun String?.toMessageType(): MessageType = when {
    this == "sms" -> MessageType.Sms
    this == "mms" -> MessageType.Mms
    else -> MessageType.Unknown
}