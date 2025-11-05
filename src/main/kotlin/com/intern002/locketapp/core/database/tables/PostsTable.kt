package com.intern002.locketapp.core.database.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object PostsTable : Table("posts") {
    val id = uuid("id").autoGenerate()
    val authorId = uuid("author_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val mediaUrl = text("media_url")
    val mediaType = text("media_type").check {
        it inList listOf("photo", "video")
    }
    val caption = text("caption").nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())

    override val primaryKey = PrimaryKey(id)
}