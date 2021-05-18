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
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import com.forest.forestchat.domain.models.message.mms.MmsPart
import com.forest.forestchat.domain.models.message.mms.MmsPartType

fun Cursor.toMmsPart() = MmsPart(
    id = getLong(getColumnIndexOrThrow(Telephony.Mms.Part._ID)),
    messageId = getLong(getColumnIndexOrThrow(Telephony.Mms.Part.MSG_ID)),
    type = (getStringOrNull(getColumnIndexOrThrow(Telephony.Mms.Part.CONTENT_TYPE))
        ?: "*/*").toMmsPartType(),
    seq = getIntOrNull(getColumnIndexOrThrow(Telephony.Mms.Part.SEQ)) ?: -1,
    name = getStringOrNull(getColumnIndexOrThrow(Telephony.Mms.Part.NAME)),
    text = getStringOrNull(getColumnIndexOrThrow(Telephony.Mms.Part.TEXT))
)

private fun String.toMmsPartType(): MmsPartType = when {
    this == "text/plain" -> MmsPartType.Text
    this == "text/x-vCard" -> MmsPartType.ContactCard
    this.startsWith("image") -> MmsPartType.Image
    this.startsWith("video") -> MmsPartType.Video
    else -> MmsPartType.All
}