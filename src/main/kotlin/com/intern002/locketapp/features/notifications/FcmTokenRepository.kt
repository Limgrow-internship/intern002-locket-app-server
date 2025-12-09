package com.intern002.locketapp.features.notifications

import com.intern002.locketapp.core.database.DatabaseFactory.dbQuery
import com.intern002.locketapp.core.database.tables.FcmTokensTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import java.util.UUID

class FcmTokenRepository {

    suspend fun addOrUpdateToken(
        userId: UUID,
        token: String,
        deviceInfo: String?
    ) = dbQuery {
        val existingToken = FcmTokensTable.select { FcmTokensTable.token eq token }.singleOrNull()

        if (existingToken != null) {
            FcmTokensTable.update({ FcmTokensTable.token eq token }) {
                it[FcmTokensTable.userId] = userId
                it[FcmTokensTable.deviceInfo] = deviceInfo
                it[FcmTokensTable.isActive] = true
            }
        } else {
            FcmTokensTable.insert {
                it[FcmTokensTable.userId] = userId
                it[FcmTokensTable.token] = token
                it[FcmTokensTable.deviceInfo] = deviceInfo
                it[FcmTokensTable.isActive] = true
            }
        }
    }

    suspend fun deactivateToken(token: String): Boolean = dbQuery {
        FcmTokensTable.update({ FcmTokensTable.token eq token }) {
            it[isActive] = false
        } > 0
    }

    suspend fun deactivateAllTokensForUser(userId: UUID): Boolean = dbQuery {
        FcmTokensTable.update({ FcmTokensTable.userId eq userId }) {
            it[isActive] = false
        } > 0
    }

    suspend fun getActiveTokensForUser(userId: UUID): List<String> = dbQuery {
        FcmTokensTable
            .select { (FcmTokensTable.userId eq userId) and (FcmTokensTable.isActive eq true) }
            .map { it[FcmTokensTable.token] }
    }
}
