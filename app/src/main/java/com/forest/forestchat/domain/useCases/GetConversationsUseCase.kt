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
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetConversationsUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {

    operator fun invoke() : String? {
        context.contentResolver.query(
            Uri.parse(
                "content://mms-sms/canonical-addresses"
            ),
            null,
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.count > 0) {
                val count = cursor.count.toString()
                while (cursor.moveToNext()) {
                    val conversationId = cursor.getString(0)
                    val recipientId = cursor.getString(1)
                }
            }
        }
        return ""
    }

}