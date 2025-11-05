package com.intern002.locketapp.core.database.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp


object UsersTable: Table("users"){

    val id = uuid("id").autoGenerate()
    val email = text("email").uniqueIndex()
    val passwordHash = text("password_hash").nullable()
    val username = text("username")
    val discriminator = integer("discriminator")
    val birthday = date("birthday")
    val provider = text("provider").default("email").check {
        it inList listOf("email","google", "facebook")
    }
    val provider_id = text("provider_id").nullable()
    val avatar_url = text("avatar_url").nullable()
    val refresh_token = text("refresh_token").nullable()
    val created_at = timestamp("created_at").defaultExpression(CurrentTimestamp())

    override val primaryKey = PrimaryKey(id)

    init{
        uniqueIndex("users_username_discriminator_unique", username, discriminator)
    }
}