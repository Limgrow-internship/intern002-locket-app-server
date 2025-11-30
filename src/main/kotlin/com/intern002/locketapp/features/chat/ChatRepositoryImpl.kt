package com.intern002.locketapp.features.chat

import com.intern002.locketapp.core.database.DatabaseFactory.dbQuery
import com.intern002.locketapp.core.database.tables.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID

class ChatRepositoryImpl : ChatRepository {
    override suspend fun getConversations(userId: UUID): List<ConversationListItemDTO> = dbQuery {
        val conversationIds = FriendshipsTable.select {
            ((FriendshipsTable.requesterId eq userId) or (FriendshipsTable.addresseeId eq userId)) and
                    (FriendshipsTable.status eq "accepted")
        }.mapNotNull { it[FriendshipsTable.conversationId] }

        val results = mutableListOf<ConversationListItemDTO>()

        for (convId in conversationIds) {
            val friendship = FriendshipsTable.select { FriendshipsTable.conversationId eq convId }.single()
            val partnerId = if (friendship[FriendshipsTable.requesterId] == userId) friendship[FriendshipsTable.addresseeId] else friendship[FriendshipsTable.requesterId]

            val partner = UsersTable.select { UsersTable.id eq partnerId }.map {
                ConversationPartnerDTO(
                    id = it[UsersTable.id],
                    username = it[UsersTable.username],
                    avatarUrl = it[UsersTable.avatarUrl]
                )
            }.single()

            val conversation = ConversationsTable.select { ConversationsTable.id eq convId }.single()

            val lastMessageRow = MessagesTable
                .select { MessagesTable.conversationId eq convId }
                .orderBy(MessagesTable.createdAt, SortOrder.DESC)
                .limit(1)
                .singleOrNull()

            val lastMessage = lastMessageRow?.let { toMessageDTO(it) }

            results.add(ConversationListItemDTO(
                conversationId = convId,
                partner = partner,
                lastMessage = lastMessage,
                createdAt = conversation[ConversationsTable.createdAt]
            ))
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
            return@dbQuery null // User is not part of this conversation
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

    private fun toMessageDTO(row: ResultRow): MessageDTO {
        return MessageDTO(
            senderId = row[MessagesTable.senderId],
            messageType = row[MessagesTable.messageType],
            content = row[MessagesTable.content],
            imageUrl = row[MessagesTable.imageUrl],
            createdAt = row[MessagesTable.createdAt]
        )
    }
}