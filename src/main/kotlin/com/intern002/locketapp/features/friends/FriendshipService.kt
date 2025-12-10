package com.intern002.locketapp.features.friends

import com.intern002.locketapp.features.notifications.NotificationService
import java.util.UUID

class FriendshipService(
    private val repository: FriendshipRepository,
    private val notificationService: NotificationService
) {

    suspend fun findUser(searcherId: UUID, username: String, discriminator: Int): FriendUserResponse? {
        val user = repository.findUserByUsernameAndDiscriminator(username, discriminator)
            ?: return null

        // Do not return the user if they are the one searching
        if (user.id == searcherId) {
            return null
        }

        // Do not return the user if they have a blocked relationship
        if (repository.isBlocked(searcherId, user.id)) {
            return null
        }

        return user.let {
            FriendUserResponse(
                id = it.id.toString(),
                username = it.username,
                discriminator = it.discriminator,
                avatarUrl = it.avatarUrl
            )
        }
    }

    suspend fun sendRequest(requesterId: UUID, addresseeUsername: String, addresseeDiscriminator: Int): Result<Friendship> {
        // Note: We're not using the findUser service method here because we need the raw user object for the ID.
        val addressee = repository.findUserByUsernameAndDiscriminator(addresseeUsername, addresseeDiscriminator)
            ?: return Result.failure(Exception("User not found."))

        if (requesterId == addressee.id) {
            return Result.failure(Exception("You cannot add yourself as a friend."))
        }
        
        if (repository.isBlocked(requesterId, addressee.id)) {
            return Result.failure(Exception("Unable to send request. This user has blocked you or you have blocked them."))
        }

        val newFriendship = repository.sendFriendRequest(requesterId, addressee.id)
            ?: return Result.failure(Exception("A friendship or request already exists."))

        // --- Send Notification ---
        notificationService.createFriendRequestNotification(requesterId, addressee.id, newFriendship.id)
        // -------------------------

        return Result.success(newFriendship)
    }

    suspend fun acceptRequest(friendshipId: UUID, currentUserId: UUID): Boolean {
        val friendship = repository.getFriendshipById(friendshipId) ?: return false

        val success = repository.acceptFriendRequest(friendshipId)
        if (success) {
            // --- Send Notification ---
            val originalRequesterId = if (friendship.requesterId == currentUserId) friendship.addresseeId else friendship.requesterId
            notificationService.createFriendAcceptNotification(originalRequesterId, currentUserId, friendshipId)
            // -------------------------
        }
        return success
    }

    suspend fun rejectRequest(friendshipId: UUID): Boolean {
        return repository.rejectFriendRequest(friendshipId)
    }

    suspend fun unfriend(userId: UUID, friendId: UUID): Boolean {
        return repository.unfriend(userId, friendId)
    }
    
    suspend fun blockFriend(blockerId: UUID, blockedId: UUID): Boolean {
        if (blockerId == blockedId) return false
        return repository.blockFriend(blockerId, blockedId)
    }

    suspend fun unblockFriend(blockerId: UUID, blockedId: UUID): Boolean {
        if (blockerId == blockedId) return false
        return repository.unblockFriend(blockerId, blockedId)
    }

    suspend fun getBlockedUsers(blockerId: UUID): List<FriendUserResponse> {
        val users = repository.getBlockedUsers(blockerId)
        return users.map { user ->
            FriendUserResponse(
                id = user.id.toString(),
                username = user.username,
                discriminator = user.discriminator,
                avatarUrl = user.avatarUrl
            )
        }
    }

    suspend fun getFriendsForUser(userId: UUID): List<FriendResponse> {
        val friends = repository.getFriends(userId)
        return friends.map { user ->
            FriendResponse(
                user = FriendUserResponse(
                    id = user.id.toString(),
                    username = user.username,
                    discriminator = user.discriminator,
                    avatarUrl = user.avatarUrl
                ),
                status = "accepted"
            )
        }
    }

    suspend fun getPendingRequestsForUser(userId: UUID): List<PendingFriendRequest> {
        return repository.getPendingRequests(userId)
    }

    suspend fun getSentRequestsForUser(userId: UUID): List<SentFriendRequestResponse> {
        val sentRequests = repository.getSentRequests(userId)
        return sentRequests.map { req ->
            SentFriendRequestResponse(
                friendshipId = req.friendshipId.toString(),
                addressee = FriendUserResponse(
                    id = req.addressee.id.toString(),
                    username = req.addressee.username,
                    discriminator = req.addressee.discriminator,
                    avatarUrl = req.addressee.avatarUrl
                ),
                status = req.status
            )
        }
    }

    suspend fun getFriendSuggestions(userId: UUID, limit: Int = 10): List<FriendUserResponse> {
        val users = repository.getFriendSuggestions(userId, limit)
        return users.map {
            FriendUserResponse(
                id = it.id.toString(),
                username = it.username,
                discriminator = it.discriminator,
                avatarUrl = it.avatarUrl
            )
        }
    }
}
