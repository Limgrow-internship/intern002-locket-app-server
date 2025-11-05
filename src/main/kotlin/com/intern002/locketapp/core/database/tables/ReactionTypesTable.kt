package com.intern002.locketapp.core.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object ReactionTypesTable : Table("reaction_types") {
    // id SERIAL PRIMARY KEY
    val id = integer("id").autoIncrement() // autoIncrement() = SERIAL

    val name = text("name").nullable()
    val emoji = text("emoji").nullable()
    val imageUrl = text("image_url").nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())

    // Khai báo PK cho cột auto-increment
    override val primaryKey = PrimaryKey(id)
}