package com.intern002.locketapp.features.friends

import java.util.UUID

class FriendshipService(private val repository: FriendshipRepository) {

    suspend fun findUser(username: String, discriminator: Int): FriendUserResponse? {
        val user = repository.findUserByUsernameAndDiscriminator(username, discriminator)
        return user?.let {
            FriendUserResponse(
                id = it.id.toString(),
                username = it.username,
                discriminator = it.discriminator,
                avatarUrl = it.avatarUrl
            )
        }
    }

    suspend fun sendRequest(requesterId: UUID, addresseeUsername: String, addresseeDiscriminator: Int): Result<Friendship> {
        val addressee = repository.findUserByUsernameAndDiscriminator(addresseeUsername, addresseeDiscriminator)
            ?: return Result.failure(Exception("User not found."))

        if (requesterId == addressee.id) {
            return Result.failure(Exception("You cannot add yourself as a friend."))
        }

        val newFriendship = repository.sendFriendRequest(requesterId, addressee.id)
            ?: return Result.failure(Exception("A friendship or request already exists."))

        return Result.success(newFriendship)
    }

    suspend fun acceptRequest(friendshipId: UUID): Boolean {
        return repository.acceptFriendRequest(friendshipId)
    }

    suspend fun rejectRequest(friendshipId: UUID): Boolean {
        return repository.rejectFriendRequest(friendshipId)
    }

    suspend fun unfriend(userId: UUID, friendId: UUID): Boolean {
        return repository.unfriend(userId, friendId)
    }

    suspend fun getFriendsForUser(userId: UUID): List<FriendUserResponse> {
        val friends = repository.getFriends(userId)
        return friends.map {
            FriendUserResponse(
                id = it.id.toString(),
                username = it.username,
                discriminator = it.discriminator,
                avatarUrl = it.avatarUrl
            )
        }
    }

    suspend fun getPendingRequestsForUser(userId: UUID): List<PendingFriendRequest> {
        return repository.getPendingRequests(userId)
    }

    suspend fun getSentRequestsForUser(userId: UUID): List<SentFriendRequestResponse> {
        val sentRequests = repository.getSentRequests(userId)
        return sentRequests.map {
            SentFriendRequestResponse(
                friendshipId = it.friendshipId.toString(),
                addressee = FriendUserResponse(
                    id = it.addressee.id.toString(),
                    username = it.addressee.username,
                    discriminator = it.addressee.discriminator,
                    avatarUrl = it.addressee.avatarUrl
                )
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
