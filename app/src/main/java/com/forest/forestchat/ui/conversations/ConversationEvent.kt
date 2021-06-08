package com.forest.forestchat.ui.conversations

import com.forest.forestchat.domain.models.Conversation
import com.forest.forestchat.domain.models.SearchConversationResult
import com.forest.forestchat.domain.models.contact.Contact

sealed class ConversationEvent {
    object RequestPermission : ConversationEvent()
    object RequestDefaultSms : ConversationEvent()
    object Loading : ConversationEvent()
    object NoConversationsData : ConversationEvent()
    object NoSearchData : ConversationEvent()
    object AdsConsentComplete : ConversationEvent()

    data class ConversationsData(
        val conversations: List<Conversation>,
        val adsActivated: Boolean
    ) : ConversationEvent()

    data class SearchData(
        val conversations: List<SearchConversationResult>,
        val contacts: List<Contact>
    ) : ConversationEvent()

    data class ShowConversationOptions(
        val showAddToContacts: Boolean,
        val showPin: Boolean,
        val showPinnedOff: Boolean,
        val showMarkAsRead: Boolean
    ) : ConversationEvent()

    data class AddContact(val address: String) : ConversationEvent()

    data class RequestDeleteDialog(val id: Long) : ConversationEvent()

}