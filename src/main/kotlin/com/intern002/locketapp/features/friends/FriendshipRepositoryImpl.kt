package com.intern002.locketapp.features.friends

import com.intern002.locketapp.core.database.DatabaseFactory.dbQuery
import com.intern002.locketapp.core.database.tables.ConversationsTable
import com.intern002.locketapp.core.database.tables.FriendshipsTable
import com.intern002.locketapp.core.database.tables.UsersTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import java.util.*

// NOTE: This should be in a dedicated models file
data class SentFriendRequest(
    val friendshipId: UUID,
    val addressee: User
)

class FriendshipRepositoryImpl : FriendshipRepository {

    override suspend fun findUserByUsernameAndDiscriminator(username: String, discriminator: Int): User? = dbQuery {
        UsersTable.select { (UsersTable.username eq username) and (UsersTable.discriminator eq discriminator) }
            .map(::rowToUser).singleOrNull()
    }

    override suspend fun sendFriendRequest(requesterId: UUID, addresseeId: UUID): Friendship? = dbQuery {
        val activeOrPendingFriendship = FriendshipsTable.select {
            (
                ((FriendshipsTable.requesterId eq requesterId) and (FriendshipsTable.addresseeId eq addresseeId)) or
                ((FriendshipsTable.requesterId eq addresseeId) and (FriendshipsTable.addresseeId eq requesterId))
            ) and (FriendshipsTable.status inList listOf("pending", "accepted"))
        }.singleOrNull()

        if (activeOrPendingFriendship != null) {
            return@dbQuery null
        }

        val rejectedFriendship = FriendshipsTable.select {
            (
                ((FriendshipsTable.requesterId eq requesterId) and (FriendshipsTable.addresseeId eq addresseeId)) or
                ((FriendshipsTable.requesterId eq addresseeId) and (FriendshipsTable.addresseeId eq requesterId))
            ) and (FriendshipsTable.status eq "rejected")
        }.singleOrNull()

        if (rejectedFriendship != null) {
            val updatedRows = FriendshipsTable.update({ FriendshipsTable.id eq rejectedFriendship[FriendshipsTable.id] }) {
                it[this.requesterId] = requesterId
                it[this.addresseeId] = addresseeId
                it[status] = "pending"
                it[updatedAt] = CurrentTimestamp()
            }
            if (updatedRows > 0) {
                FriendshipsTable.select { FriendshipsTable.id eq rejectedFriendship[FriendshipsTable.id] }.map(::rowToFriendship).singleOrNull()
            } else {
                null 
            }
        } else {
            val result = FriendshipsTable.insert {
                it[this.requesterId] = requesterId
                it[this.addresseeId] = addresseeId
                it[this.status] = "pending"
            }
            result.resultedValues?.singleOrNull()?.let(::rowToFriendship)
        }
    }

    override suspend fun acceptFriendRequest(friendshipId: UUID): Boolean = dbQuery {
        val friendship = FriendshipsTable.select { FriendshipsTable.id eq friendshipId }.singleOrNull()
            ?: return@dbQuery false

        if (friendship[FriendshipsTable.status] != "pending") {
            return@dbQuery false
        }

        val newConversationId = ConversationsTable.insert {}.resultedValues?.singleOrNull()?.get(ConversationsTable.id)
            ?: return@dbQuery false

        val updatedRows = FriendshipsTable.update({ FriendshipsTable.id eq friendshipId }) {
            it[status] = "accepted"
            it[conversationId] = newConversationId
            it[updatedAt] = CurrentTimestamp()
        }

        updatedRows > 0
    }

    override suspend fun rejectFriendRequest(friendshipId: UUID): Boolean = dbQuery {
        val updatedRows = FriendshipsTable.update({ (FriendshipsTable.id eq friendshipId) and (FriendshipsTable.status eq "pending") }) {
            it[status] = "rejected"
            it[updatedAt] = CurrentTimestamp()
        }
        updatedRows > 0
    }

    override suspend fun getFriendshipStatus(userId1: UUID, userId2: UUID): Friendship? = dbQuery {
        FriendshipsTable.select {
            ((FriendshipsTable.requesterId eq userId1) and (FriendshipsTable.addresseeId eq userId2)) or
                    ((FriendshipsTable.requesterId eq userId2) and (FriendshipsTable.addresseeId eq userId1))
        }.map(::rowToFriendship).singleOrNull()
    }

    override suspend fun getFriends(userId: UUID): List<User> = dbQuery {
        val friendIds = FriendshipsTable.select {
            ((FriendshipsTable.requesterId eq userId) or (FriendshipsTable.addresseeId eq userId)) and
                    (FriendshipsTable.status eq "accepted")
        }.map { row ->
            if (row[FriendshipsTable.requesterId] == userId) row[FriendshipsTable.addresseeId] else row[FriendshipsTable.requesterId]
        }

        if (friendIds.isEmpty()) {
            return@dbQuery emptyList()
        }

        UsersTable.select { UsersTable.id inList friendIds }.mapNotNull(::rowToUser)
    }

    override suspend fun getPendingRequests(addresseeId: UUID): List<PendingFriendRequest> = dbQuery {
        FriendshipsTable.join(UsersTable, JoinType.INNER, onColumn = FriendshipsTable.requesterId, otherColumn = UsersTable.id)
            .select {
                (FriendshipsTable.addresseeId eq addresseeId) and (FriendshipsTable.status eq "pending")
            }
            .map { row ->
                PendingFriendRequest(
                    friendshipId = row[FriendshipsTable.id],
                    requester = rowToUser(row)
                )
            }
    }

    override suspend fun getSentRequests(requesterId: UUID): List<SentFriendRequest> = dbQuery {
        FriendshipsTable.join(UsersTable, JoinType.INNER, onColumn = FriendshipsTable.addresseeId, otherColumn = UsersTable.id)
            .select {
                (FriendshipsTable.requesterId eq requesterId) and (FriendshipsTable.status eq "pending")
            }
            .map { row ->
                SentFriendRequest(
                    friendshipId = row[FriendshipsTable.id],
                    addressee = rowToUser(row)
                )
            }
    }

    private fun rowToUser(row: ResultRow): User {
        return User(
            id = row[UsersTable.id],
            email = row[UsersTable.email],
            username = row[UsersTable.username],
            discriminator = row[UsersTable.discriminator],
            birthday = row[UsersTable.birthday].toString(),
            avatarUrl = row[UsersTable.avatarUrl]
        )
    }

    private fun rowToFriendship(row: ResultRow): Friendship {
        return Friendship(
            id = row[FriendshipsTable.id],
            requesterId = row[FriendshipsTable.requesterId],
            addresseeId = row[FriendshipsTable.addresseeId],
            status = row[FriendshipsTable.status],
            conversationId = row[FriendshipsTable.conversationId]
        )
    }
}