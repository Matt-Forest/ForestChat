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
import com.forest.forestchat.domain.models.message.mms.MessageMms
import com.forest.forestchat.domain.models.message.mms.MmsPart
import com.google.android.mms.pdu_alt.EncodedStringValue
import com.google.android.mms.pdu_alt.PduPersister

fun Cursor.toMms(parts: List<MmsPart>) = MessageMms(
    subject = getString(getColumnIndex(Telephony.Mms.SUBJECT))
        ?.takeIf { it.isNotBlank() }
        ?.let(PduPersister::getBytes)
        ?.let {
            EncodedStringValue(
                getInt(getColumnIndex(Telephony.Mms.SUBJECT_CHARSET)),
                it
            ).string
        },
    parts = parts,
)