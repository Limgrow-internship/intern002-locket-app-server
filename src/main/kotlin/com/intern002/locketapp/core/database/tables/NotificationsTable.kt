package com.intern002.locketapp.core.database.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object NotificationsTable : Table("notifications") {
    val id = uuid("id").autoGenerate()
    val recipientId =
        uuid(
            "recipient_id",
        ).references(UsersTable.id, onDelete = ReferenceOption.CASCADE).index("idx_notifications_recipient")
    val senderId = uuid("sender_id").references(UsersTable.id).nullable()
    val type = text("type")
    val title = text("title").nullable()
    val message = text("message").nullable()
    val entityId = uuid("entity_id").nullable()
    val isRead = bool("is_read").default(false)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())

    override val primaryKey = PrimaryKey(id)
}
