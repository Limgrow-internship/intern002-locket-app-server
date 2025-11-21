package com.intern002.locketapp.features.users

import com.intern002.locketapp.core.utils.Hashing
import com.intern002.locketapp.core.utils.InvalidDateFormatException
import com.intern002.locketapp.core.utils.UserNotFoundException
import com.intern002.locketapp.features.auth.AuthRepository
import kotlinx.datetime.LocalDate
import java.util.UUID

class UserService(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val hashing: Hashing
) {

    suspend fun getProfile(userId: String): UserProfileResponse {
        val uuid = UUID.fromString(userId)
        val user = authRepository.findById(uuid) ?: throw UserNotFoundException()

        return UserProfileResponse(
            id = user.id.toString(),
            email = user.email,
            username = user.username,
            discriminator = user.discriminator,
            avatarUrl = user.avatarUrl,
            birthday = user.birthday.toString()
        )
    }

    suspend fun updateUser(userId: String, request: UpdateUserRequest): UserProfileResponse {
        val uuid = UUID.fromString(userId)
        val currentUser = authRepository.findById(uuid) ?: throw UserNotFoundException()

        val newEmail = request.email?.takeIf { it != currentUser.email }
        val newUsername = request.username?.takeIf { it != currentUser.username }
        val newPasswordHash = request.password?.let { hashing.hash(it) }

        val newBirthday = request.birthday?.let {
            try {
                LocalDate.parse(it)
            } catch (_: Exception) {
                throw InvalidDateFormatException()
            }
        }

        userRepository.updateUser(
            uuid,
            newEmail,
            newUsername,
            newPasswordHash,
            newBirthday,
            request.avatarUrl
        )

        val updated = authRepository.findById(uuid) ?: throw UserNotFoundException()

        return UserProfileResponse(
            id = updated.id.toString(),
            email = updated.email,
            username = updated.username,
            discriminator = updated.discriminator,
            avatarUrl = updated.avatarUrl,
            birthday = updated.birthday.toString()
        )
    }
}