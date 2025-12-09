package com.intern002.locketapp.features.notifications

import com.intern002.locketapp.features.auth.AuthRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.UUID

class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val authRepository: AuthRepository,
    private val fcmTokenRepository: FcmTokenRepository,
    private val fcmService: FCMService
) {

    suspend fun createNewMessageNotification(senderId: UUID, recipientId: UUID, conversationId: UUID, messageContent: String) = coroutineScope {
        val sender = authRepository.findById(senderId) ?: return@coroutineScope

        val title = sender.username
        val message = messageContent

        launch {
            notificationRepository.createNotification(
                recipientId = recipientId,
                senderId = senderId,
                type = NotificationType.NEW_MESSAGE,
                title = title,
                message = message,
                entityId = conversationId
            )
        }

        launch {
            val activeTokens = fcmTokenRepository.getActiveTokensForUser(recipientId)
            val payload = NotificationPayload(type = NotificationType.NEW_MESSAGE, entityId = conversationId.toString())
            activeTokens.forEach { token ->
                fcmService.sendNotification(
                    fcmToken = token,
                    title = title,
                    body = message,
                    payload = payload
                )
            }
        }
    }

    suspend fun createFriendRequestNotification(requesterId: UUID, addresseeId: UUID, friendshipId: UUID) = coroutineScope {
        val requester = authRepository.findById(requesterId) ?: return@coroutineScope

        val title = "New Friend Request"
        val message = "${requester.username} sent you a friend request."

        launch {
            notificationRepository.createNotification(
                recipientId = addresseeId,
                senderId = requesterId,
                type = NotificationType.FRIEND_REQUEST,
                title = title,
                message = message,
                entityId = friendshipId
            )
        }

        launch {
            val activeTokens = fcmTokenRepository.getActiveTokensForUser(addresseeId)
            val payload = NotificationPayload(type = NotificationType.FRIEND_REQUEST, entityId = friendshipId.toString())
            activeTokens.forEach { token ->
                fcmService.sendNotification(
                    fcmToken = token,
                    title = title,
                    body = message,
                    payload = payload
                )
            }
        }
    }

    suspend fun createFriendAcceptNotification(originalRequesterId: UUID, acceptingUserId: UUID, friendshipId: UUID) = coroutineScope {
        val acceptingUser = authRepository.findById(acceptingUserId) ?: return@coroutineScope

        val title = "Friend Request Accepted"
        val message = "${acceptingUser.username} accepted your friend request."

        launch {
            notificationRepository.createNotification(
                recipientId = originalRequesterId,
                senderId = acceptingUserId,
                type = NotificationType.FRIEND_ACCEPT,
                title = title,
                message = message,
                entityId = friendshipId
            )
        }

        launch {
            val activeTokens = fcmTokenRepository.getActiveTokensForUser(originalRequesterId)
            val payload = NotificationPayload(type = NotificationType.FRIEND_ACCEPT, entityId = friendshipId.toString())
            activeTokens.forEach { token ->
                fcmService.sendNotification(
                    fcmToken = token,
                    title = title,
                    body = message,
                    payload = payload
                )
            }
        }
    }

    suspend fun getNotifications(userId: UUID): List<NotificationResponse> {
        return notificationRepository.getNotificationsForUser(userId)
    }

    suspend fun markNotificationAsRead(notificationId: UUID): Boolean {
        return notificationRepository.markAsRead(notificationId)
    }

    suspend fun markAllAsRead(userId: UUID): Boolean {
        return notificationRepository.markAllAsRead(userId)
    }
}
