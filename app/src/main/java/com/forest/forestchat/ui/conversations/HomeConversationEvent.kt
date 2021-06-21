package com.forest.forestchat.ui.conversations

import com.forest.forestchat.domain.models.Conversation
import com.forest.forestchat.domain.models.SearchConversationResult
import com.forest.forestchat.domain.models.contact.Contact

sealed class HomeConversationEvent {
    object RequestPermission : HomeConversationEvent()
    object RequestDefaultSms : HomeConversationEvent()
    object Loading : HomeConversationEvent()
    object NoConversationsData : HomeConversationEvent()
    object NoSearchData : HomeConversationEvent()
    object AdsConsentComplete : HomeConversationEvent()

    data class GoToConversation(
        val conversation: Conversation
    ) : HomeConversationEvent()

    data class ConversationsData(
        val conversations: List<Conversation>,
        val adsActivated: Boolean
    ) : HomeConversationEvent()

    data class SearchData(
        val conversations: List<SearchConversationResult>,
        val contacts: List<Contact>
    ) : HomeConversationEvent()

    data class ShowConversationOptions(
        val showAddToContacts: Boolean,
        val showPin: Boolean,
        val showPinnedOff: Boolean,
        val showMarkAsRead: Boolean
    ) : HomeConversationEvent()

    data class AddContact(val address: String) : HomeConversationEvent()

    data class RequestDeleteDialog(val id: Long) : HomeConversationEvent()

}