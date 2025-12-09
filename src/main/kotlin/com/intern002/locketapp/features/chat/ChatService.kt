package com.intern002.locketapp.features.chat

import com.intern002.locketapp.core.utils.NotMemberOfConversationException
import com.intern002.locketapp.features.notifications.NotificationService
import io.ktor.server.plugins.*
import java.util.UUID

class ChatService(
    private val chatRepository: ChatRepository,
    private val notificationService: NotificationService
) {
    suspend fun getConversations(userId: UUID): List<ConversationListItemDTO> {
        return chatRepository.getConversations(userId)
    }

    suspend fun saveMessage(senderId: UUID, request: SendMessageRequest): MessageDTO {
        validateRequest(request)
        val newMessage = chatRepository.saveMessage(senderId, request)
            ?: throw NotMemberOfConversationException()

        val recipientId = chatRepository.getPartnerId(request.conversationId, senderId)
        if (recipientId != null) {
            val messageContent = when (newMessage.messageType) {
                "text" -> newMessage.content ?: ""
                "image" -> "Sent an image"
                "sticker" -> "Sent a sticker"
                else -> "Sent a message"
            }
            notificationService.createNewMessageNotification(
                senderId = senderId,
                recipientId = recipientId,
                conversationId = request.conversationId,
                messageContent = messageContent
            )
        }

        return newMessage
    }

    suspend fun getMessages(userId: UUID, conversationId: UUID, page: Int, pageSize: Int): List<MessageDTO> {
        return chatRepository.getMessages(userId, conversationId, page, pageSize)
    }

    suspend fun markConversationAsRead(userId: UUID, conversationId: UUID): Int {
        return chatRepository.markMessagesAsRead(conversationId, userId)
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
