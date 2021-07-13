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
package com.forest.forestchat.extensions

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import com.zhuinden.eventemitter.EventSource
import com.zhuinden.liveevent.observe

fun <T> Fragment.observe(liveData: LiveData<T>, observer: (T) -> Unit) {
    liveData.observe(viewLifecycleOwner) { data: T? ->
        // data could be null when live data obtained via SavedStateHandle
        // because value is set with 'null' when we don't use initial value
        if (data != null) {
            // observer will be called when value is set only.
            observer(data)
        }
    }
}

fun <T : Any> Fragment.observeEvents(eventSource: EventSource<T>, observer: (T) -> Unit) {
    eventSource.observe(viewLifecycleOwner) { data: T? ->
        // data could be null when live data obtained via SavedStateHandle
        // because value is set with 'null' when we don't use initial value
        if (data != null) {
            // observer will be called when value is set only.
            observer(data)
        }
    }
}