package com.intern002.locketapp.features.friends

import com.intern002.locketapp.core.utils.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Friendship(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    @Serializable(with = UUIDSerializer::class)
    val requesterId: UUID,
    @Serializable(with = UUIDSerializer::class)
    val addresseeId: UUID,
    val status: String,
    @Serializable(with = UUIDSerializer::class)
    val conversationId: UUID?
)

@Serializable
data class PendingFriendRequest(
    @Serializable(with = UUIDSerializer::class)
    val friendshipId: UUID,
    val requester: PublicUser,
    val status: String
)
