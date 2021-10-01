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

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.BlockedNumberContract
import androidx.annotation.RequiresApi
import com.forest.forestchat.domain.models.BlockedNumber
import com.forest.forestchat.extensions.getLongValue
import com.forest.forestchat.extensions.getStringValue
import com.forest.forestchat.extensions.queryCursor
import com.forest.forestchat.extensions.trimToComparableNumber
import com.forest.forestchat.utils.isNougatPlus
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetBlockedNumbersFromProviderUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    @SuppressLint("NewApi")
    operator fun invoke(): List<BlockedNumber> = when (isNougatPlus()) {
        true -> {
            val blockedNumbers = mutableListOf<BlockedNumber>()
            val uri = BlockedNumberContract.BlockedNumbers.CONTENT_URI
            val projection = arrayOf(
                BlockedNumberContract.BlockedNumbers.COLUMN_ID,
                BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER,
                BlockedNumberContract.BlockedNumbers.COLUMN_E164_NUMBER
            )

            context.queryCursor(uri, projection) { cursor ->
                val id = cursor.getLongValue(BlockedNumberContract.BlockedNumbers.COLUMN_ID)
                val number =
                    cursor.getStringValue(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER)
                        ?: ""
                val normalizedNumber =
                    cursor.getStringValue(BlockedNumberContract.BlockedNumbers.COLUMN_E164_NUMBER)
                        ?: number
                val comparableNumber = normalizedNumber.trimToComparableNumber()

                val blockedNumber = BlockedNumber(id, number, normalizedNumber, comparableNumber)
                blockedNumbers.add(blockedNumber)
            }

            blockedNumbers
        }
        false -> listOf()
    }

}