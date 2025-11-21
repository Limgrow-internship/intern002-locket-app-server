package com.intern002.locketapp.features.auth

import com.intern002.locketapp.core.database.DatabaseFactory.dbQuery
import com.intern002.locketapp.core.database.tables.UsersTable
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID

data class User(
    val id: UUID,
    val email: String,
    val username: String,
    val passwordHash: String?,
    val discriminator: Int,
    val avatarUrl: String?,
    val birthday: LocalDate,
    val provider: String,
    val providerId: String?
)

interface AuthRepository {
    suspend fun findByEmail(email: String): User?
    suspend fun findById(userId: UUID): User?
    suspend fun findByProviderId(providerId: String): User?
    suspend fun findByUsernameAndDiscriminator(username: String, discriminator: Int): User?
    suspend fun createUser(
        email: String,
        username: String,
        passwordHash: String?,
        birthday: LocalDate, //
        discriminator: Int,
        provider: String,
        providerId: String?
    ): User?

    suspend fun linkGoogleAccount(userId: UUID, providerId: String): Boolean
    suspend fun updateRefreshToken(userId: UUID, refreshToken: String?): Boolean
    suspend fun findUserByRefreshToken(refreshToken: String): User?
}

class AuthRepositoryImpl : AuthRepository {

    private fun toUser(row: ResultRow): User = User(
        id = row[UsersTable.id],
        email = row[UsersTable.email],
        username = row[UsersTable.username],
        passwordHash = row[UsersTable.passwordHash],
        discriminator = row[UsersTable.discriminator],
        avatarUrl = row[UsersTable.avatarUrl],
        birthday = row[UsersTable.birthday],
        provider = row[UsersTable.provider],
        providerId = row[UsersTable.providerId]
    )

    override suspend fun findByEmail(email: String): User? = dbQuery {
        UsersTable.select { UsersTable.email eq email }
            .map(::toUser)
            .singleOrNull()
    }

    override suspend fun findById(userId: UUID): User? = dbQuery {
        UsersTable.select { UsersTable.id eq userId }
            .map(::toUser)
            .singleOrNull()
    }

    override suspend fun findByProviderId(providerId: String): User? = dbQuery {
        UsersTable.select { (UsersTable.provider eq "google") and (UsersTable.providerId eq providerId) }
            .map(::toUser)
            .singleOrNull()
    }

    override suspend fun findByUsernameAndDiscriminator(username: String, discriminator: Int): User? = dbQuery {
        UsersTable.select { (UsersTable.username eq username) and (UsersTable.discriminator eq discriminator) }
            .map(::toUser)
            .singleOrNull()
    }

    override suspend fun createUser(
        email: String,
        username: String,
        passwordHash: String?,
        birthday: LocalDate,
        discriminator: Int,
        provider: String,
        providerId: String?
    ): User? = dbQuery {
        val insert = UsersTable.insert {
            it[UsersTable.email] = email
            it[UsersTable.username] = username
            it[UsersTable.passwordHash] = passwordHash
            it[UsersTable.birthday] = birthday
            it[UsersTable.discriminator] = discriminator
            it[UsersTable.provider] = provider
            it[UsersTable.providerId] = providerId
        }
        insert.resultedValues?.singleOrNull()?.let(::toUser)
    }

    override suspend fun linkGoogleAccount(userId: UUID, providerId: String): Boolean = dbQuery {
        UsersTable.update({ UsersTable.id eq userId }) {
            it[UsersTable.provider] = "google"
            it[UsersTable.providerId] = providerId
        } > 0
    }

    override suspend fun updateRefreshToken(userId: UUID, refreshToken: String?): Boolean = dbQuery {
        UsersTable.update({ UsersTable.id eq userId }) {
            it[UsersTable.refreshToken] = refreshToken
        } > 0
    }

    override suspend fun findUserByRefreshToken(refreshToken: String): User? = dbQuery {
        UsersTable.select { UsersTable.refreshToken eq refreshToken }
            .map(::toUser)
            .singleOrNull()
    }
}
