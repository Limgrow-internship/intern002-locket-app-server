package com.intern002.locketapp.features.friends

import com.intern002.locketapp.core.utils.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class PublicUser(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val username: String,
    val discriminator: Int,
    val avatarUrl: String?
)

interface FriendshipRepository {

    suspend fun sendFriendRequest(requesterId: UUID, addresseeId: UUID): Friendship?

    suspend fun acceptFriendRequest(friendshipId: UUID): Boolean

    suspend fun rejectFriendRequest(friendshipId: UUID): Boolean

    suspend fun unfriend(userId: UUID, friendId: UUID): Boolean

    suspend fun getFriendshipStatus(userId1: UUID, userId2: UUID): Friendship?

    suspend fun getFriends(userId: UUID): List<PublicUser>

    suspend fun findUserByUsernameAndDiscriminator(username: String, discriminator: Int): PublicUser?

    suspend fun getPendingRequests(addresseeId: UUID): List<PendingFriendRequest>

    suspend fun getSentRequests(requesterId: UUID): List<SentFriendRequest>

    suspend fun getFriendSuggestions(userId: UUID, limit: Int): List<PublicUser>
}
