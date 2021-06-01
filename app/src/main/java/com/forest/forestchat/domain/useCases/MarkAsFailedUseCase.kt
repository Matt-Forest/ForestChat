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

import android.content.ContentValues
import android.content.Context
import android.provider.Telephony
import com.forest.forestchat.domain.models.message.MessageBox
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarkAsFailedUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getMessageByIdUseCase: GetMessageByIdUseCase,
    private val updateMessageUseCase: UpdateMessageUseCase
) {

    suspend operator fun invoke(messageId: Long, resultCode: Int) {
        getMessageByIdUseCase(messageId)?.let { message ->
            updateMessageUseCase(
                message.copy(
                    box = MessageBox.Failed,
                    sms = message.sms?.copy(
                        errorCode = resultCode
                    )
                )
            )

            // Update the message in the native ContentProvider
            val values = ContentValues()
            values.put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_FAILED)
            values.put(Telephony.Sms.ERROR_CODE, resultCode)
            message.getUri()?.let { uri ->
                context.contentResolver.update(uri, values, null, null)
            }
        }
    }

}