package com.intern002.locketapp.core.database.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object MessageReactionsTable : Table("message_reactions") {
    val id = uuid("id").autoGenerate()
    val messageId = uuid("message_id").references(MessagesTable.id, onDelete = ReferenceOption.CASCADE).index()
    val reactorId = uuid("reactor_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val reactionTypeId =
        integer("reaction_type_id").references(ReactionTypesTable.id, onDelete = ReferenceOption.CASCADE)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())

    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex("message_reactions_message_id_reactor_id_unique", messageId, reactorId)
    }
}