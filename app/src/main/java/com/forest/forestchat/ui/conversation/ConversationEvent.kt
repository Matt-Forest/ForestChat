package com.forest.forestchat.ui.conversation

import com.forest.forestchat.domain.models.message.Message

sealed class ConversationEvent {
    object Empty : ConversationEvent()
    object Loading : ConversationEvent()

    data class BaseData(val title: String) : ConversationEvent()
    data class Data(val messages: List<Message>) : ConversationEvent()

}
