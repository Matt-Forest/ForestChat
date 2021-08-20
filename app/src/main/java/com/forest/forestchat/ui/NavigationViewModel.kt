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
package com.forest.forestchat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forest.forestchat.domain.useCases.GetOrCreateConversationByThreadIdUseCase
import com.forest.forestchat.manager.PermissionsManager
import com.forest.forestchat.ui.splash.models.SplashEvent
import com.zhuinden.eventemitter.EventEmitter
import com.zhuinden.eventemitter.EventSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val getOrCreateConversationByThreadIdUseCase: GetOrCreateConversationByThreadIdUseCase,
    private val permissionsManager: PermissionsManager
) : ViewModel() {

    private val eventEmitter = EventEmitter<NavigationEvent>()
    fun eventSource(): EventSource<NavigationEvent> = eventEmitter

    private var threadId: Long? = null

    fun setRedirection(threadId: Long) {
        this.threadId = threadId
    }

    fun consumeRedirection() {
        threadId?.let { id ->
            viewModelScope.launch(Dispatchers.IO) {
                when {
                    !permissionsManager.isDefaultSms() -> {
                        withContext(Dispatchers.Main) {
                            eventEmitter.emit(NavigationEvent.RequestDefaultSms)
                        }
                    }
                    !permissionsManager.hasReadSms() || !permissionsManager.hasContacts() -> {
                        withContext(Dispatchers.Main) {
                            eventEmitter.emit(NavigationEvent.RequestPermission)
                        }
                    }
                    else -> {
                        getOrCreateConversationByThreadIdUseCase(id)?.let { conversation ->
                            withContext(Dispatchers.Main) {
                                threadId = null
                                eventEmitter.emit(NavigationEvent.GoToConversation(conversation))
                            }
                        }
                    }
                }
            }
        }
    }

}