package com.intern002.locketapp.core.database.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object MessagesTable : Table("messages") {
    val id = uuid("id").autoGenerate()
    val conversationId =
        uuid(
            "conversation_id",
        ).references(ConversationsTable.id, onDelete = ReferenceOption.CASCADE).index("idx_messages_conversation")
    val senderId = uuid("sender_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val content = text("content").nullable()
    val imageUrl = text("image_url").nullable()
    val messageType = text("message_type").default("text").check { it inList listOf("text", "image", "sticker") }
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())

    override val primaryKey = PrimaryKey(id)
}