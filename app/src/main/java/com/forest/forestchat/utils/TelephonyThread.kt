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
package com.forest.forestchat.utils

import android.content.Context
import android.database.sqlite.SqliteWrapper
import android.net.Uri
import android.os.Build
import android.provider.BaseColumns
import android.provider.Telephony
import android.util.Patterns
import java.util.regex.Pattern

object TelephonyThread {

    fun getOrCreateThreadId(context: Context, recipients: Collection<String>): Long {
        return if (Build.VERSION.SDK_INT >= 23) {
            Telephony.Threads.getOrCreateThreadId(context, recipients.toSet())
        } else {
            val uriBuilder = Uri.parse("content://mms-sms/threadID").buildUpon()

            recipients
                .map { recipient -> if (isEmailAddress(recipient)) extractAddress(recipient) else recipient }
                .forEach { recipient -> uriBuilder.appendQueryParameter("recipient", recipient) }

            val uri = uriBuilder.build()

            SqliteWrapper.query(
                context,
                context.contentResolver,
                uri,
                arrayOf(BaseColumns._ID),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    return cursor.getLong(0)
                }
            }

            throw IllegalArgumentException("Unable to find or allocate a thread ID.")
        }
    }

    private fun extractAddress(address: String?): String? {
        address?.let { s ->
            val match = Pattern.compile("\\s*(\"[^\"]*\"|[^<>\"]+)\\s*<([^<>]+)>\\s*").matcher(s)
            return when (match.matches()) {
                true -> match.group(2)
                false -> s
            }
        }

        return address
    }

    private fun isEmailAddress(address: String?): Boolean {
        if (!address.isNullOrEmpty()) {
            extractAddress(address)?.let { s ->
                val match = Patterns.EMAIL_ADDRESS.matcher(s)
                return match.matches()
            }
        }
        return false
    }

}