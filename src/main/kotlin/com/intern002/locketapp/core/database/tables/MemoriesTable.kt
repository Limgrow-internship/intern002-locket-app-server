package com.intern002.locketapp.core.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date // 👈 QUAN TRỌNG: Phải là cái này
import org.jetbrains.exposed.sql.javatime.timestamp // 👈 QUAN TRỌNG

object MemoriesTable : Table("memories") {
    val id = uuid("id")
    val postId = uuid("post_id")
    val authorId = uuid("author_id")
    val mediaUrl = text("media_url")
    val date = date("date")
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}