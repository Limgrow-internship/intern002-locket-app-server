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
import kotlin.system.exitProcess

object DatabaseFactory {
    private val logger = LoggerFactory.getLogger("DatabaseFactory")
    private var dataSource: HikariDataSource? = null

    fun init(
        databaseUrl: String,
        databaseUser: String,
        databasePassword: String,
    ) {
        logger.info("Initializing DB connection...")
        try {
            dataSource = createHikariDataSource(databaseUrl, databaseUser, databasePassword)
            Database.connect(dataSource!!)
            logger.info("Connected to Supabase PostgreSQL successfully!")

            transaction {
                logger.info("Creating or verifying database schema...")
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
                logger.info("Database schema verification/creation complete.")
            }
        } catch (e: Exception) {
            logger.error("FATAL: Failed to connect or initialize DB schema. Exiting application.", e)
            exitProcess(1)
        }
    }

    fun close() {
        logger.info("Closing database connection pool.")
        dataSource?.close()
    }

    private fun createHikariDataSource(
        url: String,
        user: String,
        password: String,
        maxPoolSize: Int = 1,
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
                dataSourceProperties["sslmode"] = "require"
                dataSourceProperties["prepareThreshold"] = 0
                validate()
            }

        return HikariDataSource(config)
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO) { block() }
}