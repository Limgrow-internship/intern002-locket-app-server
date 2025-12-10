package com.intern002.locketapp.core.database.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object FriendshipsTable : Table("friendships") {
    val id = uuid("id").autoGenerate()

    val requesterId = uuid("requester_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val addresseeId = uuid("addressee_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val status =
        text("status").default("pending").check {
            it inList listOf("pending", "accepted", "rejected", "blocked")
        }

    val conversationId =
        uuid("conversation_id").references(
            ConversationsTable.id,
            onDelete = ReferenceOption.SET_NULL,
        ).nullable().uniqueIndex()

    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp())

    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex("friendships_requester_addressee_unique", requesterId, addresseeId)
    }
}
