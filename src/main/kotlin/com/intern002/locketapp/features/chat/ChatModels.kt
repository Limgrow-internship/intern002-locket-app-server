package com.intern002.locketapp.features.chat

import com.intern002.locketapp.core.utils.UUIDSerializer
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class ConversationPartnerDTO(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val username: String,
    val avatarUrl: String?
)

@Serializable
data class MessageDTO(
    @Serializable(with = UUIDSerializer::class)
    val senderId: UUID,
    val messageType: String,
    val content: String?,
    val imageUrl: String?,
    val createdAt: Instant,
    val isRead: Boolean
)

@Serializable
data class ConversationListItemDTO(
    @Serializable(with = UUIDSerializer::class)
    val conversationId: UUID,
    val partner: ConversationPartnerDTO,
    val lastMessage: MessageDTO?,
    val unreadCount: Int,
    val createdAt: Instant,
    val friendshipStatus: String
)

@Serializable
data class SendMessageRequest(
    @Serializable(with = UUIDSerializer::class)
    val conversationId: UUID,
    val messageType: String,
    val content: String? = null,
    val imageUrl: String? = null
)
