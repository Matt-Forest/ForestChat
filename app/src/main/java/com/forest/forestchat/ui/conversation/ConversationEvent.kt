package com.forest.forestchat.ui.conversation

import android.telephony.SubscriptionInfo
import com.forest.forestchat.domain.models.Recipient
import com.forest.forestchat.domain.models.message.Message

sealed class ConversationEvent {
    object Empty : ConversationEvent()
    object Loading : ConversationEvent()

    data class BaseData(val title: String) : ConversationEvent()
    data class Data(
        val messages: List<Message>,
        val recipients: List<Recipient>,
        val subscriptionsInfo: List<SubscriptionInfo>
    ) : ConversationEvent()

}
