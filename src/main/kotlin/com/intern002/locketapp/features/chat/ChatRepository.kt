package com.intern002.locketapp.features.chat

import java.util.UUID

interface ChatRepository {
    suspend fun getConversations(userId: UUID): List<ConversationListItemDTO>
    suspend fun saveMessage(senderId: UUID, request: SendMessageRequest): MessageDTO?
    suspend fun getMessages(userId: UUID, conversationId: UUID, page: Int, pageSize: Int): List<MessageDTO>
}
