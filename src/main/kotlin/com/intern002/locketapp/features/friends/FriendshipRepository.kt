package com.intern002.locketapp.features.friends

import java.util.UUID

interface FriendshipRepository {

    /**
     * Creates a new friend request.
     */
    suspend fun sendFriendRequest(requesterId: UUID, addresseeId: UUID): Friendship?

    /**
     * Accepts a friend request.
     * This also creates a new conversation between the users.
     */
    suspend fun acceptFriendRequest(friendshipId: UUID): Boolean

    /**
     * Rejects a friend request.
     */
    suspend fun rejectFriendRequest(friendshipId: UUID): Boolean

    /**
     * Gets the friendship status between two users.
     */
    suspend fun getFriendshipStatus(userId1: UUID, userId2: UUID): Friendship?

    /**
     * Gets a list of a user's friends (accepted friendships).
     */
    suspend fun getFriends(userId: UUID): List<User>

    /**
     * Finds a user by their username and discriminator.
     */
    suspend fun findUserByUsernameAndDiscriminator(username: String, discriminator: Int): User?

    /**
     * Gets all pending friend requests for a specific user.
     */
    suspend fun getPendingRequests(addresseeId: UUID): List<PendingFriendRequest>

    /**
     * Gets all sent friend requests from a specific user.
     */
    suspend fun getSentRequests(requesterId: UUID): List<SentFriendRequest> // Added this
}
