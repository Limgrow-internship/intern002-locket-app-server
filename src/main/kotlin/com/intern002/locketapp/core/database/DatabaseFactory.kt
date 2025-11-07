package com.intern002.locketapp.core.database

import com.intern002.locketapp.core.database.tables.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

object DatabaseFactory {
    private val logger = LoggerFactory.getLogger("DatabaseFactory")

    fun init(
        databaseUrl: String,
        databaseUser: String,
        databasePassword: String,
    ) {
        try {
            val dataSource = createHikariDataSource(databaseUrl, databaseUser, databasePassword)
            Database.connect(dataSource)
            logger.info("Connected to Supabase PostgreSQL successfully!")

            transaction {
                SchemaUtils.create(
                    UsersTable,
                    FriendshipsTable,
                    PostsTable,
                    PostRecipientsTable,
                    ReactionTypesTable,
                    PostReactionsTable,
                    NotificationsTable,
                    FcmTokensTable,
                    ConversationsTable,
                    MessagesTable,
                )
            }
        } catch (e: Exception) {
            logger.error("Failed to connect to Supabase DB: ${e.message}", e)
        }
    }

    private fun createHikariDataSource(
        url: String,
        user: String,
        password: String,
        maxPoolSize: Int = 10,
    ): HikariDataSource {
        val config =
            HikariConfig().apply {
                driverClassName = "org.postgresql.Driver"
                jdbcUrl = url
                username = user
                this.password = password
                maximumPoolSize = maxPoolSize
                isAutoCommit = false
                transactionIsolation = "TRANSACTION_REPEATABLE_READ"
                dataSourceProperties["sslmode"] = "require" // ⚡ Bắt buộc với Supabase
                validate()
            }
        return HikariDataSource(config)
    }

    // Hàm tiện ích chạy truy vấn trong coroutine context
    suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO) { block() }
}
