package com.intern002.locketapp.features.notifications

import com.intern002.locketapp.core.database.DatabaseFactory.dbQuery
import com.intern002.locketapp.core.database.tables.NotificationsTable
import com.intern002.locketapp.core.database.tables.UsersTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID

interface NotificationRepository {
    suspend fun createNotification(
        recipientId: UUID,
        senderId: UUID?,
        type: NotificationType,
        title: String?,
        message: String?,
        entityId: UUID?
    ): NotificationResponse?

    suspend fun getNotificationsForUser(userId: UUID): List<NotificationResponse>

    suspend fun markAsRead(notificationId: UUID): Boolean

    suspend fun markAllAsRead(userId: UUID): Boolean
}

class NotificationRepositoryImpl : NotificationRepository {
    override suspend fun createNotification(
        recipientId: UUID,
        senderId: UUID?,
        type: NotificationType,
        title: String?,
        message: String?,
        entityId: UUID?
    ): NotificationResponse? = dbQuery {
        val notificationId = NotificationsTable.insert {
            it[this.recipientId] = recipientId
            it[this.senderId] = senderId
            it[this.type] = type.name
            it[this.title] = title
            it[this.message] = message
            it[this.entityId] = entityId
        } get NotificationsTable.id

        // Re-fetch the notification to get all columns, including DB-generated ones like createdAt
        NotificationsTable.join(UsersTable, JoinType.LEFT, onColumn = NotificationsTable.senderId, otherColumn = UsersTable.id)
            .select { NotificationsTable.id eq notificationId }
            .singleOrNull()
            ?.let { toNotificationResponse(it) }
    }

    override suspend fun getNotificationsForUser(userId: UUID): List<NotificationResponse> = dbQuery {
        NotificationsTable.join(UsersTable, JoinType.LEFT, onColumn = NotificationsTable.senderId, otherColumn = UsersTable.id)
            .select { NotificationsTable.recipientId eq userId }
            .orderBy(NotificationsTable.createdAt, SortOrder.DESC)
            .map { toNotificationResponse(it) }
    }

    override suspend fun markAsRead(notificationId: UUID): Boolean = dbQuery {
        NotificationsTable.update({ NotificationsTable.id eq notificationId }) {
            it[isRead] = true
        } > 0
    }

    override suspend fun markAllAsRead(userId: UUID): Boolean = dbQuery {
        NotificationsTable.update({ (NotificationsTable.recipientId eq userId) and (NotificationsTable.isRead eq false) }) {
            it[isRead] = true
        } > 0
    }

    private fun toNotificationResponse(row: ResultRow): NotificationResponse {
        val sender = row.getOrNull(UsersTable.id)?.let {
            NotificationSender(
                id = it,
                username = row[UsersTable.username],
                avatarUrl = row[UsersTable.avatarUrl]
            )
        }

        return NotificationResponse(
            id = row[NotificationsTable.id],
            type = row[NotificationsTable.type],
            title = row[NotificationsTable.title],
            message = row[NotificationsTable.message],
            entityId = row[NotificationsTable.entityId],
            isRead = row[NotificationsTable.isRead],
            createdAt = row[NotificationsTable.createdAt],
            sender = sender
        )
    }
}
