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
package com.forest.forestchat.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.forest.forestchat.app.TransversalBusEvent
import com.forest.forestchat.domain.useCases.SyncDataUseCase
import com.forest.forestchat.manager.ForestChatShortCutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

@AndroidEntryPoint
class DefaultSmsChangedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var syncDataUseCase: SyncDataUseCase

    @Inject
    lateinit var forestChatShortCutManager: ForestChatShortCutManager

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.getBooleanExtra(
                Telephony.Sms.Intents.EXTRA_IS_DEFAULT_SMS_APP,
                false
            ) == true
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                EventBus.getDefault().post(TransversalBusEvent.DefaultSmsChangedEvent.Load)
                syncDataUseCase()
                forestChatShortCutManager.updateBadge()
                EventBus.getDefault().post(TransversalBusEvent.DefaultSmsChangedEvent.Complete)
            }
        }
    }

}