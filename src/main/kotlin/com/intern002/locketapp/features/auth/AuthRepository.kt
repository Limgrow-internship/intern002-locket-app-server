package com.intern002.locketapp.features.auth

import com.intern002.locketapp.core.database.DatabaseFactory.dbQuery
import com.intern002.locketapp.core.database.tables.UsersTable
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.util.UUID

data class User(
    val id: UUID,
    val email: String,
    val passwordHash: String?,
    val username: String,
    val discriminator: Int,
    val birthday: LocalDate,
    val provider: String,
    val providerId: String?
)

class AuthRepository {

    suspend fun findUserByEmail(email: String): User? {
        return dbQuery {
            UsersTable
                .select { UsersTable.email eq email.lowercase() }
                .map {row -> row.toUser()}
                .singleOrNull()
        }
    }

    suspend fun createUser(request: AuthRequest, passwordHash: String): User? {
        val birthdayDate: LocalDate
        try {
            birthdayDate = LocalDate.parse(request.birthday)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid birthday format. Use YYYY-MM-DD")
        }
        return dbQuery {
            val insertedId = UsersTable.insert{
                it[UsersTable.email] = request.email.lowercase()
                it[UsersTable.passwordHash] = passwordHash
                it[UsersTable.username] = request.username
                it[UsersTable.discriminator] = request.discriminator
                it[UsersTable.birthday] = birthdayDate
            } get UsersTable.id
            findUserByEmail(request.email.lowercase())
        }
    }

    private fun ResultRow.toUser(): User = User(
        id = this[UsersTable.id],
        email = this[UsersTable.email],
        passwordHash = this[UsersTable.passwordHash] ?: "",
        username = this[UsersTable.username],
        discriminator = this[UsersTable.discriminator],
        birthday = this [UsersTable.birthday],
        provider = this[UsersTable.provider],
        providerId = this[UsersTable.providerId]
    )
}