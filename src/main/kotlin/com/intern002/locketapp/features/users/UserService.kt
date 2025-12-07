package com.intern002.locketapp.features.users

import com.intern002.locketapp.core.utils.EmailAlreadyExistsException
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

    suspend fun verifyPassword(userId: String, request: VerifyPasswordRequest): VerifyPasswordResponse {
        val uuid = UUID.fromString(userId)
        val user = authRepository.findById(uuid) ?: throw UserNotFoundException()

        val isCorrect = user.passwordHash != null && hashing.verify(request.password, user.passwordHash!!)

        return VerifyPasswordResponse(isCorrect)
    }

    suspend fun updateUser(userId: String, request: UpdateUserRequest): UserProfileResponse {
        val uuid = UUID.fromString(userId)
        val currentUser = authRepository.findById(uuid) ?: throw UserNotFoundException()

        val newEmail = request.email?.takeIf { it.isNotBlank() && it != currentUser.email }
        val newUsername = request.username?.takeIf { it.isNotBlank() && it != currentUser.username }
        val newPasswordHash = request.password?.let { hashing.hash(it) }
        val newBirthday = request.birthday?.let {
            try {
                LocalDate.parse(it)
            } catch (_: Exception) {
                throw InvalidDateFormatException()
            }
        }?.takeIf { it != currentUser.birthday }

        newEmail?.let {
            if (authRepository.findByEmail(it) != null) {
                throw EmailAlreadyExistsException()
            }
        }

        val hasChanges = newEmail != null || newUsername != null || newPasswordHash != null || newBirthday != null || request.avatarUrl != currentUser.avatarUrl

        if (hasChanges) {
            userRepository.updateUser(
                uuid,
                newEmail,
                newUsername,
                newPasswordHash,
                newBirthday,
                request.avatarUrl
            )
        }

        val updatedUser = if (hasChanges) {
            authRepository.findById(uuid) ?: throw UserNotFoundException()
        } else {
            currentUser
        }

        return UserProfileResponse(
            id = updatedUser.id.toString(),
            email = updatedUser.email,
            username = updatedUser.username,
            discriminator = updatedUser.discriminator,
            avatarUrl = updatedUser.avatarUrl,
            birthday = updatedUser.birthday.toString()
        )
    }
}
