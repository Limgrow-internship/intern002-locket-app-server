package com.intern002.locketapp.features.chat

import com.intern002.locketapp.core.utils.NotMemberOfConversationException
import io.ktor.server.plugins.*
import java.util.UUID

class ChatService(private val chatRepository: ChatRepository) {
    suspend fun getConversations(userId: UUID): List<ConversationListItemDTO> {
        return chatRepository.getConversations(userId)
    }

    suspend fun saveMessage(senderId: UUID, request: SendMessageRequest): MessageDTO {
        validateRequest(request)
        return chatRepository.saveMessage(senderId, request)
            ?: throw NotMemberOfConversationException()
    }

    suspend fun getMessages(userId: UUID, conversationId: UUID, page: Int, pageSize: Int): List<MessageDTO> {
        return chatRepository.getMessages(userId, conversationId, page, pageSize)
    }

    private fun validateRequest(request: SendMessageRequest) {
        when (request.messageType) {
            "text" -> if (request.content.isNullOrBlank()) throw BadRequestException("Content cannot be empty for text messages.")
            "image" -> if (request.imageUrl.isNullOrBlank()) throw BadRequestException("Image URL cannot be empty for image messages.")
            "sticker" -> if (request.content.isNullOrBlank()) throw BadRequestException("Content must contain sticker ID for sticker messages.")
            else -> throw BadRequestException("Invalid message type.")
        }
    }
}