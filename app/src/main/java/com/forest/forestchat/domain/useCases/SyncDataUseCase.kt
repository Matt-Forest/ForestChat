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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.forest.forestchat.localStorage.sharedPrefs.LastSyncSharedPrefs
import kotlinx.coroutines.delay
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncDataUseCase @Inject constructor(
    private val syncMessagesUseCase: SyncMessagesUseCase,
    private val syncContactsUseCase: SyncContactsUseCase,
    private val syncConversationsUseCase: SyncConversationsUseCase,
    private val lastSyncSharedPrefs: LastSyncSharedPrefs
) {

    private val syncFinished = MutableLiveData(false)
    fun syncFinished(): LiveData<Boolean> = syncFinished

    private var syncInProgress = false

    suspend operator fun invoke() {
        if (!syncInProgress) {
            syncFinished.postValue(false)
            syncInProgress = true
            syncMessagesUseCase()
            syncContactsUseCase()

            // As long as the conversations aren't synchronize we stay here.
            // Not the best thing :/
            while (syncConversationsUseCase()) {
                delay(100)
            }

            lastSyncSharedPrefs.set(Date().time)
            syncFinished.postValue(true)
            syncInProgress = false
        }
    }

}