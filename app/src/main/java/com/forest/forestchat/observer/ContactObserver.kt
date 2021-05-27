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
package com.forest.forestchat.observer

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract

class ContactObserver(
    val context: Context,
    val onContactChanged: () -> Unit
) : ContentObserver(Handler(Looper.getMainLooper())) {

    private var isStarted: Boolean = false

    companion object {
        private val URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
    }

    fun start() {
        if (!isStarted) {
            isStarted = true
            context.contentResolver.registerContentObserver(URI, true, this)
        }
    }

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        if (isStarted) {
            isStarted = false

            context.contentResolver.unregisterContentObserver(this)
            onContactChanged()
        }
    }

}