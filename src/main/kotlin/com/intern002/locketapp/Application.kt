package com.intern002.locketapp

import com.intern002.locketapp.core.database.DatabaseFactory
import com.intern002.locketapp.plugins.configureDependencyInjection
import com.intern002.locketapp.plugins.configureMonitoring
import com.intern002.locketapp.plugins.configureRouting
import com.intern002.locketapp.plugins.configureSecurity
import com.intern002.locketapp.plugins.configureSerialization
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.*
import org.slf4j.LoggerFactory

fun main(args: Array<String>) {
    dotenv {
        ignoreIfMissing = true
        systemProperties = true
    }
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val logger = LoggerFactory.getLogger("Application")

    val env =
        dotenv {
            ignoreIfMissing = true
        }

    val dbUrl = env["DATABASE_URL"] ?: System.getenv("DATABASE_URL")
    val dbUser = env["DATABASE_USER"] ?: System.getenv("DATABASE_USER")
    val dbPassword = env["DATABASE_PASSWORD"] ?: System.getenv("DATABASE_PASSWORD")

    logger.info("Database URL: $dbUrl")
    logger.info("Connecting to database...")

    DatabaseFactory.init(dbUrl ?: error("Missing DATABASE_URL"), dbUser ?: "", dbPassword ?: "")

    logger.info("Database connected successfully!")

    configureMonitoring()
    configureDependencyInjection()
    configureSecurity()
    configureSerialization()
    configureRouting()
}
