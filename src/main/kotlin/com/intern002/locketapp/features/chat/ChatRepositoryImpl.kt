package com.intern002.locketapp.features.chat

import com.intern002.locketapp.core.database.DatabaseFactory.dbQuery
import com.intern002.locketapp.core.database.tables.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID

class ChatRepositoryImpl : ChatRepository {
    override suspend fun getPartnerId(conversationId: UUID, senderId: UUID): UUID? = dbQuery {
        val friendship = FriendshipsTable.select { FriendshipsTable.conversationId eq conversationId }.singleOrNull()
            ?: return@dbQuery null

        if (friendship[FriendshipsTable.requesterId] == senderId) {
            friendship[FriendshipsTable.addresseeId]
        } else {
            friendship[FriendshipsTable.requesterId]
        }
    }

    override suspend fun getConversations(userId: UUID): List<ConversationListItemDTO> = dbQuery {
        val friendships = FriendshipsTable
            .slice(FriendshipsTable.conversationId, FriendshipsTable.requesterId, FriendshipsTable.addresseeId)
            .select {
                ((FriendshipsTable.requesterId eq userId) or (FriendshipsTable.addresseeId eq userId)) and
                        (FriendshipsTable.status eq "accepted")
            }
            .map {
                val convId = it[FriendshipsTable.conversationId]
                val partnerId = if (it[FriendshipsTable.requesterId] == userId) it[FriendshipsTable.addresseeId] else it[FriendshipsTable.requesterId]
                convId to partnerId
            }

        val convIds = friendships.mapNotNull { it.first }
        if (convIds.isEmpty()) return@dbQuery emptyList()

        val partnerIds = friendships.map { it.second }

        val partners = UsersTable
            .select { UsersTable.id inList partnerIds }
            .associate {
                it[UsersTable.id] to ConversationPartnerDTO(
                    id = it[UsersTable.id],
                    username = it[UsersTable.username],
                    avatarUrl = it[UsersTable.avatarUrl]
                )
            }

        val conversations = ConversationsTable
            .slice(ConversationsTable.id, ConversationsTable.createdAt)
            .select { ConversationsTable.id inList convIds }
            .associate { it[ConversationsTable.id] to it[ConversationsTable.createdAt] }

        val maxCreatedAt = MessagesTable.createdAt.max()
        val lastMessageSubQuery = MessagesTable
            .slice(MessagesTable.conversationId, maxCreatedAt)
            .select { MessagesTable.conversationId inList convIds }
            .groupBy(MessagesTable.conversationId)

        val lastMessagePairs = lastMessageSubQuery.mapNotNull { row ->
            row[maxCreatedAt]?.let { maxTime ->
                row[MessagesTable.conversationId] to maxTime
            }
        }

        val lastMessages = if (lastMessagePairs.isNotEmpty()) {
            MessagesTable
                .select { (MessagesTable.conversationId to MessagesTable.createdAt) inList lastMessagePairs }
                .associate { it[MessagesTable.conversationId] to toMessageDTO(it) }
        } else {
            emptyMap()
        }
        
        val unreadCounts = MessagesTable
            .slice(MessagesTable.conversationId, MessagesTable.id.count())
            .select { (MessagesTable.conversationId inList convIds) and (MessagesTable.isRead eq false) and (MessagesTable.senderId neq userId) }
            .groupBy(MessagesTable.conversationId)
            .associate { it[MessagesTable.conversationId] to it[MessagesTable.id.count()].toInt() }

        val results = friendships.mapNotNull { (convId, partnerId) ->
            if (convId == null) return@mapNotNull null
            val partner = partners[partnerId] ?: return@mapNotNull null
            val conversationCreatedAt = conversations[convId] ?: return@mapNotNull null
            val lastMessage = lastMessages[convId]

            ConversationListItemDTO(
                conversationId = convId,
                partner = partner,
                lastMessage = lastMessage,
                unreadCount = unreadCounts[convId] ?: 0,
                createdAt = conversationCreatedAt
            )
        }

        results.sortedByDescending { it.lastMessage?.createdAt ?: it.createdAt }
    }

    override suspend fun saveMessage(senderId: UUID, request: SendMessageRequest): MessageDTO? = dbQuery {
        val friendship = FriendshipsTable.select {
            (FriendshipsTable.conversationId eq request.conversationId) and
                    ((FriendshipsTable.requesterId eq senderId) or (FriendshipsTable.addresseeId eq senderId)) and
                    (FriendshipsTable.status eq "accepted")
        }.singleOrNull()

        if (friendship == null) {
            return@dbQuery null
        }

        val newMessageId = MessagesTable.insert {
            it[MessagesTable.conversationId] = request.conversationId
            it[MessagesTable.senderId] = senderId
            it[messageType] = request.messageType
            it[content] = request.content
            it[imageUrl] = request.imageUrl
        } get MessagesTable.id

        MessagesTable.select { MessagesTable.id eq newMessageId }
            .singleOrNull()
            ?.let { toMessageDTO(it) }
    }

    override suspend fun getMessages(userId: UUID, conversationId: UUID, page: Int, pageSize: Int): List<MessageDTO> = dbQuery {
        val isParticipant = FriendshipsTable.select {
            (FriendshipsTable.conversationId eq conversationId) and
                    ((FriendshipsTable.requesterId eq userId) or (FriendshipsTable.addresseeId eq userId)) and
                    (FriendshipsTable.status eq "accepted")
        }.count() > 0

        if (!isParticipant) {
            return@dbQuery emptyList()
        }

        val messageRows = MessagesTable.select { MessagesTable.conversationId eq conversationId }
            .orderBy(MessagesTable.createdAt, SortOrder.DESC)
            .limit(pageSize, offset = ((page - 1) * pageSize).toLong())
            .toList()

        messageRows.map { toMessageDTO(it) }
    }

    override suspend fun markMessagesAsRead(conversationId: UUID, userId: UUID): Int = dbQuery {
        MessagesTable.update(
            where = {
                (MessagesTable.conversationId eq conversationId) and
                        (MessagesTable.senderId neq userId) and // Only mark messages sent by the other person
                        (MessagesTable.isRead eq false)
            }
        ) {
            it[isRead] = true
        }
    }

    private fun toMessageDTO(row: ResultRow): MessageDTO {
        return MessageDTO(
            senderId = row[MessagesTable.senderId],
            messageType = row[MessagesTable.messageType],
            content = row[MessagesTable.content],
            imageUrl = row[MessagesTable.imageUrl],
            createdAt = row[MessagesTable.createdAt],
            isRead = row[MessagesTable.isRead]
        )
    }
}
