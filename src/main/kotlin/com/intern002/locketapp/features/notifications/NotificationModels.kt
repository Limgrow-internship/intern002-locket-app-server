package com.intern002.locketapp.features.notifications

import com.intern002.locketapp.core.utils.UUIDSerializer
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class NotificationResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val type: String,
    val title: String?,
    val message: String?,
    @Serializable(with = UUIDSerializer::class)
    val entityId: UUID?,
    val isRead: Boolean,
    val createdAt: Instant,
    val sender: NotificationSender?
)

@Serializable
data class NotificationSender(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val username: String,
    val avatarUrl: String?
)
