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
package com.forest.forestchat.domain.useCases

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import com.forest.forestchat.domain.mappers.toMessage
import com.forest.forestchat.localStorage.database.daos.MessageDao
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncMessagesUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val messageDao: MessageDao
) {

    suspend operator fun invoke() {
        messageDao.deleteAll()
        context.contentResolver.query(
            Uri.parse("content://mms-sms/complete-conversations"),
            arrayOf(
                Telephony.MmsSms.TYPE_DISCRIMINATOR_COLUMN,
                Telephony.MmsSms._ID,
                Telephony.Mms.DATE,
                Telephony.Mms.DATE_SENT,
                Telephony.Mms.READ,
                Telephony.Mms.THREAD_ID,
                Telephony.Mms.LOCKED,

                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.SEEN,
                Telephony.Sms.TYPE,
                Telephony.Sms.STATUS,
                Telephony.Sms.ERROR_CODE,

                Telephony.Mms.SUBJECT,
                Telephony.Mms.SUBJECT_CHARSET,
                Telephony.Mms.SEEN,
                Telephony.Mms.MESSAGE_TYPE,
                Telephony.Mms.MESSAGE_BOX,
                Telephony.Mms.DELIVERY_REPORT,
                Telephony.Mms.READ_REPORT,
                Telephony.MmsSms.PendingMessages.ERROR_TYPE,
                Telephony.Mms.STATUS,
                Telephony.Mms.SUBSCRIPTION_ID
            ),
            null,
            null,
            "normalized_date desc"
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                messageDao.insert(cursor.toMessage(context))
            }
        }
    }

}