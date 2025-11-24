package com.intern002.locketapp.features.friends

import com.intern002.locketapp.core.utils.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * The standard PUBLIC data transfer object for a User.
 * This model should be used across all features when returning public user info.
 */
@Serializable
data class User(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val email: String?,
    val username: String,
    val discriminator: Int,
    val birthday: String,
    val avatarUrl: String?
)

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
    val requester: User
)
