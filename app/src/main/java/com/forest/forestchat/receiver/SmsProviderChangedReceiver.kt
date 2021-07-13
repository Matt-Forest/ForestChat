package com.forest.forestchat.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.forest.forestchat.domain.useCases.SyncMessageFromUriUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * When the SMS contentprovider is changed by a process other than us, we need to sync the Uri that
 * was changed.
 *
 * This can happen if a message is sent through something like Pushbullet or Google Assistant.
 *
 * This only works on API 24+, so to fully solve this problem we'll need a smarter way of running
 * partial syncs on older devices.
 *
 * https://developer.android.com/reference/android/provider/Telephony.Sms.Intents.html#ACTION_EXTERNAL_PROVIDER_CHANGE
 */
@AndroidEntryPoint
class SmsProviderChangedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var syncMessageFromUriUseCase: SyncMessageFromUriUseCase

    override fun onReceive(context: Context?, intent: Intent?) {
        // Obtain the uri for the changed data
        // If the value is null, don't continue
        intent?.data?.let { uri ->
            CoroutineScope(Dispatchers.IO).launch {
                syncMessageFromUriUseCase(uri)
            }
        }
    }

}