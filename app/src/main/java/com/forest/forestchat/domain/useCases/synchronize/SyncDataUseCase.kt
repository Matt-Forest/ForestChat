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
package com.forest.forestchat.domain.useCases.synchronize

import com.forest.forestchat.localStorage.sharedPrefs.LastSyncSharedPrefs
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

    suspend operator fun invoke() {
        syncMessagesUseCase()
        syncContactsUseCase()
        syncConversationsUseCase()
        lastSyncSharedPrefs.set(Date().time)
    }

}