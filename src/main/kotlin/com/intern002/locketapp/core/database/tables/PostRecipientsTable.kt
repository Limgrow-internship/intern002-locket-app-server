package com.intern002.locketapp.core.database.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object PostRecipientsTable : Table("post_recipients") {
    val id = uuid("id").autoGenerate()
    val postId = uuid("post_id").references(PostsTable.id, onDelete = ReferenceOption.CASCADE)
    val recipientId = uuid("recipient_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())

    override val primaryKey = PrimaryKey(id)

    // UNIQUE (post_id, recipient_id)
    init {
        uniqueIndex("post_recipients_post_id_recipient_id_unique", postId, recipientId)
    }
}