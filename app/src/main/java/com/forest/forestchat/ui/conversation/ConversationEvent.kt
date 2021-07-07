package com.forest.forestchat.ui.conversation

import android.telephony.SubscriptionInfo
import com.forest.forestchat.domain.models.Recipient
import com.forest.forestchat.domain.models.message.Message
import com.forest.forestchat.ui.common.avatar.AvatarType
import com.forest.forestchat.ui.common.media.Media
import java.io.File

sealed class ConversationEvent {
    object Loading : ConversationEvent()
    object RequestStoragePermission : ConversationEvent()

    data class Data(
        val messages: List<Message>,
        val recipients: List<Recipient>,
        val subscriptionsInfo: List<SubscriptionInfo>
    ) : ConversationEvent()

    data class BaseData(val title: String) : ConversationEvent()
    data class ViewFile(val file: File) : ConversationEvent()
    data class ShowMessageOptions(val canCopy: Boolean) : ConversationEvent()
    data class ShowMessageDetails(val details: String) : ConversationEvent()
    data class ShowGallery(val mediaSelected: Media, val medias: List<Media>) : ConversationEvent()
    data class Empty(
        val avatarType: AvatarType,
        val title: String,
        val phone: String?
    ) : ConversationEvent()

}
