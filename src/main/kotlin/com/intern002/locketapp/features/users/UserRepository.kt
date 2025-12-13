package com.intern002.locketapp.features.users

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
    val birthday: LocalDate
)

interface UserRepository {
    suspend fun findByUsername(username: String): User?
    suspend fun findByEmail(email: String): User?
    suspend fun findById(userId: UUID): User?
    suspend fun findByUsernameAndDiscriminator(username: String, discriminator: Int): User?
    suspend fun createUser(email: String, username: String, passwordHash: String, birthday: LocalDate, discriminator: Int): User?
    suspend fun updateUser(userId: UUID, email: String?, username: String?, passwordHash: String?, birthday: LocalDate?, avatarUrl: String?): Boolean
    suspend fun setAvatarUrl(userId: UUID, avatarUrl: String?): Boolean
    suspend fun deleteUser(userId: UUID): Boolean
}

class UserRepositoryImpl : UserRepository {

    private fun toUser(row: ResultRow): User = User(
        id = row[UsersTable.id],
        email = row[UsersTable.email],
        username = row[UsersTable.username],
        passwordHash = row[UsersTable.passwordHash],
        discriminator = row[UsersTable.discriminator],
        avatarUrl = row[UsersTable.avatarUrl],
        birthday = row[UsersTable.birthday]
    )

    override suspend fun findByUsername(username: String): User? = dbQuery {
        UsersTable.select { UsersTable.username eq username }
            .map(::toUser)
            .firstOrNull()
    }

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

    override suspend fun findByUsernameAndDiscriminator(username: String, discriminator: Int): User? = dbQuery {
        UsersTable.select { (UsersTable.username eq username) and (UsersTable.discriminator eq discriminator) }
            .map(::toUser)
            .singleOrNull()
    }

    override suspend fun createUser(email: String, username: String, passwordHash: String, birthday: LocalDate, discriminator: Int): User? = dbQuery {
        val insertStatement = UsersTable.insert {
            it[UsersTable.email] = email
            it[UsersTable.username] = username
            it[UsersTable.passwordHash] = passwordHash
            it[UsersTable.birthday] = birthday
            it[UsersTable.discriminator] = discriminator
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::toUser)
    }

    override suspend fun updateUser(userId: UUID, email: String?, username: String?, passwordHash: String?, birthday: LocalDate?, avatarUrl: String?): Boolean = dbQuery {
        UsersTable.update({ UsersTable.id eq userId }) {
            email?.let { newEmail -> it[UsersTable.email] = newEmail }
            username?.let { newUsername -> it[UsersTable.username] = newUsername }
            passwordHash?.let { newPasswordHash -> it[UsersTable.passwordHash] = newPasswordHash }
            birthday?.let { newBirthday -> it[UsersTable.birthday] = newBirthday }
            avatarUrl?.let { newAvatarUrl -> it[UsersTable.avatarUrl] = newAvatarUrl }
        } > 0
    }

    override suspend fun setAvatarUrl(userId: UUID, avatarUrl: String?): Boolean = dbQuery {
        UsersTable.update({ UsersTable.id eq userId }) {
            it[UsersTable.avatarUrl] = avatarUrl
        } > 0
    }

    override suspend fun deleteUser(userId: UUID): Boolean = dbQuery {
        UsersTable.deleteWhere { id eq userId } > 0
    }
}