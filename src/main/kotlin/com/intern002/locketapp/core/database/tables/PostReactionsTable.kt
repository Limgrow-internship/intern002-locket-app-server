package com.intern002.locketapp.core.database.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object PostReactionsTable: Table("post_reactions") {

    val id = uuid("id").autoGenerate()
    val postId = uuid("post_id").references(PostsTable.id, onDelete = ReferenceOption.CASCADE)
    val reactorId = uuid("reactor_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)

    val reactionTypeId = integer("reaction_type_id").references(ReactionTypesTable.id).nullable()

    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())

    override val primaryKey = PrimaryKey(id)

    // UNIQUE (post_id, reactor_id)
    init {
        uniqueIndex("post_reactions_post_id_reactor_id_unique", postId, reactorId)
    }
}