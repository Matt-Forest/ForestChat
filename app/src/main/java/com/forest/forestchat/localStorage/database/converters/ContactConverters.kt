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
package com.forest.forestchat.localStorage.database.converters

import androidx.room.TypeConverter
import com.forest.forestchat.domain.models.contact.PhoneNumber
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object ContactConverters {

    class PhoneNumberConverter {

        private val adapter: JsonAdapter<List<PhoneNumber>> =
            Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
                .adapter(
                    Types.newParameterizedType(
                        List::class.java,
                        PhoneNumber::class.java
                    )
                )

        @TypeConverter
        fun itemsToString(items: List<PhoneNumber>?): String? = items?.let {
            adapter.toJson(items)
        }

        @TypeConverter
        fun itemsFromString(json: String?): List<PhoneNumber>? = json?.let {
            adapter.fromJson(json)
        }

    }

}