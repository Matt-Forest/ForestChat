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
import android.os.Build
import androidx.annotation.RequiresApi
import com.forest.forestchat.extensions.trimToComparableNumber
import com.forest.forestchat.utils.isNougatPlus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IsBlockedNumbersFromProviderUseCase @Inject constructor(
    private val getBlockedNumbersFromProviderUseCase: GetBlockedNumbersFromProviderUseCase
) {

    @SuppressLint("NewApi")
    operator fun invoke(number: String): Boolean = when (isNougatPlus()) {
        true -> {
            val blockedNumbers = getBlockedNumbersFromProviderUseCase()

            val numberToCompare = number.trimToComparableNumber()
            blockedNumbers.map { it.numberToCompare }
                .contains(numberToCompare) || blockedNumbers.map { it.number }
                .contains(numberToCompare)
        }
        false -> false
    }

}