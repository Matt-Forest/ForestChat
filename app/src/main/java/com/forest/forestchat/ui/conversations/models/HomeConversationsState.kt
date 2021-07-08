package com.forest.forestchat.ui.conversations.models

import androidx.annotation.StringRes
import com.forest.forestchat.domain.models.Conversation
import com.forest.forestchat.domain.models.SearchConversationResult
import com.forest.forestchat.domain.models.contact.Contact

sealed class HomeConversationsState {

    object RequestPermission : HomeConversationsState()

    data class Empty(@StringRes val label: Int) : HomeConversationsState()
    data class Conversations(val conversations: List<Conversation>) : HomeConversationsState()
    data class Search(
        val conversations: List<SearchConversationResult>,
        val contacts: List<Contact>
    ) : HomeConversationsState()

}
