package com.intern002.locketapp.features.notifications

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.MessagingErrorCode
import org.slf4j.LoggerFactory

data class NotificationPayload(
    val type: NotificationType,
    val entityId: String
)

enum class NotificationType {
    FRIEND_REQUEST,
    FRIEND_ACCEPT,
    NEW_MESSAGE,
    POST_REACTION
}

class FCMService(
    private val fcmTokenRepository: FcmTokenRepository
) {
    private val logger = LoggerFactory.getLogger(FCMService::class.java)

    suspend fun sendNotification(
        fcmToken: String,
        title: String,
        body: String,
        payload: NotificationPayload
    ) {
        val message = Message.builder()
            .setToken(fcmToken)
            .putAllData(mapOf(
                "type" to payload.type.name,
                "title" to title,
                "body" to body,
                "entityId" to payload.entityId
            ))
            .build()

        try {
            val response = FirebaseMessaging.getInstance().send(message)
            logger.info("Successfully sent data message to token $fcmToken: $response")
        } catch (e: FirebaseMessagingException) {
            logger.error("Failed to send FCM message to token $fcmToken", e)

            val errorCode = e.messagingErrorCode
            if (errorCode == MessagingErrorCode.UNREGISTERED || errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
                logger.info("Deactivating invalid FCM token: $fcmToken")
                fcmTokenRepository.deactivateToken(fcmToken)
            }
        }
    }
}
