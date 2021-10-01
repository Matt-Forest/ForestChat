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
package com.forest.forestchat.ui.recipients

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.forest.forestchat.domain.models.Recipient
import com.forest.forestchat.extensions.getNavigationInput
import com.forest.forestchat.ui.recipients.models.RecipientsEvent
import com.forest.forestchat.ui.recipients.models.RecipientsInput
import com.zhuinden.eventemitter.EventEmitter
import com.zhuinden.eventemitter.EventSource
import javax.inject.Inject

class RecipientsViewModel @Inject constructor(
    handle: SavedStateHandle
) : ViewModel() {

    private val recipientsData = MutableLiveData<List<Recipient>>()
    fun recipientsData(): LiveData<List<Recipient>> = recipientsData

    private val eventEmitter = EventEmitter<RecipientsEvent>()
    fun eventSource(): EventSource<RecipientsEvent> = eventEmitter

    private val recipients = handle.getNavigationInput<RecipientsInput>().recipients

    init {
        recipientsData.value = recipients
    }

    fun recipientClick(recipientId: Long) {
        recipients.find { it.id == recipientId }?.let { recipient ->
            eventEmitter.emit(
                RecipientsEvent.ShowContact(
                    recipient.contact?.lookupKey,
                    recipient.address
                )
            )
        }
    }

}