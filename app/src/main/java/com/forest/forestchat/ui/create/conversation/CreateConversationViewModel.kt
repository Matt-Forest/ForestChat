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
package com.forest.forestchat.ui.create.conversation

import android.telephony.PhoneNumberUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forest.forestchat.domain.useCases.GetContactByIdUseCase
import com.forest.forestchat.domain.useCases.GetContactsUseCase
import com.forest.forestchat.domain.useCases.GetOrCreateConversationByAddressesUseCase
import com.forest.forestchat.domain.useCases.SearchContactsUseCase
import com.forest.forestchat.ui.create.conversation.models.ContactSearch
import com.forest.forestchat.ui.create.conversation.models.ContactSelected
import com.forest.forestchat.ui.create.conversation.models.CreateConversationButtonState
import com.forest.forestchat.ui.create.conversation.models.CreateConversationEvent
import com.zhuinden.eventemitter.EventEmitter
import com.zhuinden.eventemitter.EventSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CreateConversationViewModel @Inject constructor(
    private val getContactsUseCase: GetContactsUseCase,
    private val getContactByIdUseCase: GetContactByIdUseCase,
    private val searchContactsUseCase: SearchContactsUseCase,
    private val getOrCreateConversationByAddressesUseCase: GetOrCreateConversationByAddressesUseCase
) : ViewModel() {

    private val contactsSearch = MutableLiveData<List<ContactSearch>>()
    fun contactsSearch(): LiveData<List<ContactSearch>> = contactsSearch

    private val contactsSelected = MutableLiveData<MutableList<ContactSelected>>()
    fun contactsSelected(): LiveData<MutableList<ContactSelected>> = contactsSelected

    private val buttonState = MutableLiveData<CreateConversationButtonState>()
    fun buttonState(): LiveData<CreateConversationButtonState> = buttonState

    private val newRecipient = MutableLiveData<String>()
    fun newRecipient(): LiveData<String> = newRecipient

    private val eventEmitter = EventEmitter<CreateConversationEvent>()
    fun eventSource(): EventSource<CreateConversationEvent> = eventEmitter

    private var search: String = ""

    init {
        getContacts()
    }

    private fun getContacts() {
        viewModelScope.launch(Dispatchers.IO) {
            val contactIds = contactsSelected.value?.mapNotNull { it.contact?.id } ?: listOf()
            val data = getContactsUseCase()?.map { contact ->
                ContactSearch(contact, contactIds.contains(contact.id))
            }
            contactsSearch.postValue(data)
        }
    }

    fun contactCheck(contactId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val contactIds = contactsSelected.value?.mapNotNull { it.contact?.id } ?: listOf()
            when (contactIds.contains(contactId)) {
                true -> removeRecipient(contactId)
                false -> addRecipient(contactId)
            }
            onSearchChange(search)
            updateButtonState()
        }
    }

    private suspend fun addRecipient(contactId: Long) {
        val contacts = when (contactsSelected.value == null) {
            true -> mutableListOf()
            false -> contactsSelected.value!!
        }
        contacts.add(ContactSelected(getContactByIdUseCase(contactId), null))
        contactsSelected.postValue(contacts.toMutableList())
    }

    private fun removeRecipient(contactId: Long) {
        val contacts = when (contactsSelected.value == null) {
            true -> mutableListOf()
            false -> contactsSelected.value!!
        }
        contacts.remove(contacts.find { it.contact != null && it.contact.id == contactId })
        contactsSelected.postValue(contacts.toMutableList())
    }

    fun addNewRecipient() {
        contactsSelected.value?.let { data ->
            data.add(ContactSelected(null, search))
            contactsSelected.postValue(data.toMutableList())
        }
        updateButtonState()
    }

    fun removeRecipientsSelect(index: Int) {
        contactsSelected.value?.let { data ->
            if (data[index].contact != null) { // contact case
                removeRecipient(data[index].contact?.id ?: 0L)
                updateSearch()
            } else { // new recipient case
                data.removeAt(index)
                contactsSelected.postValue(data.toMutableList())
            }
        }
        updateButtonState()
    }

    fun onSearchChange(search: String) {
        this.search = search
        val contactIds = contactsSelected.value?.mapNotNull { it.contact?.id } ?: listOf()
        viewModelScope.launch(Dispatchers.IO) {
            if (search.isNotBlank()) {
                val data = searchContactsUseCase(search)?.map { contact ->
                    ContactSearch(contact, contactIds.contains(contact.id))
                }
                contactsSearch.postValue(data)
            } else {
                getContacts()
            }
        }
        updateNewRecipient(search)
    }

    private fun updateNewRecipient(search: String) {
        newRecipient.postValue(
            when {
                PhoneNumberUtils.isGlobalPhoneNumber(search) -> search
                else -> ""
            }
        )
    }

    private fun updateSearch() {
        val contactIdsSelected = contactsSelected.value?.map { it.contact?.id } ?: listOf()
        val searchData = contactsSearch.value?.map { contact ->
            ContactSearch(contact.contact, contactIdsSelected.contains(contact.contact.id))
        }
        contactsSearch.postValue(searchData)
    }

    private fun updateButtonState() {
        buttonState.postValue(
            when (contactsSelected.value?.count() ?: 0) {
                0 -> CreateConversationButtonState.None
                1 -> CreateConversationButtonState.Create
                else -> CreateConversationButtonState.Next
            }
        )
    }

    fun create() {
        when (contactsSelected.value?.count() ?: 0) {
            0 -> null
            1 -> {
                viewModelScope.launch(Dispatchers.IO) {
                    val contact = contactsSelected.value?.get(0)?.contact
                    val address = contactsSelected.value?.get(0)?.newAddress
                    getOrCreateConversationByAddressesUseCase(
                        listOf(
                            when {
                                contact != null -> contact.getDefaultNumber()?.address
                                    ?: contact.numbers[0].address
                                address != null -> address
                                else -> ""
                            }
                        )
                    )?.let { conversation ->
                        withContext(Dispatchers.Main) {
                            eventEmitter.emit(CreateConversationEvent.GoToConversation(conversation))
                        }
                    }
                }
            }
            else -> {
                val contacts = contactsSelected.value
                    ?.filter { it.contact != null }
                    ?.map { it.contact!! }
                val newRecipients = contactsSelected.value
                    ?.filter { it.newAddress != null }
                    ?.map { it.newAddress!! }
                eventEmitter.emit(CreateConversationEvent.GoToCreateGroup(contacts, newRecipients))
            }
        }
    }

}