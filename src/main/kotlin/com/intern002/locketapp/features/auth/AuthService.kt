package com.intern002.locketapp.features.auth

import com.intern002.locketapp.core.security.TokenClaim
import com.intern002.locketapp.core.security.TokenConfig
import com.intern002.locketapp.core.security.TokenProvider
import com.intern002.locketapp.core.utils.* // Import all custom exceptions
import kotlinx.datetime.LocalDate
import java.util.UUID
import kotlin.random.Random

class AuthService(
    private val authRepository: AuthRepository,
    private val tokenProvider: TokenProvider,
    private val hashing: Hashing
) {

    suspend fun register(request: RegisterRequest): AuthResponse {
        if (authRepository.findByEmail(request.email) != null) {
            throw EmailAlreadyExistsException()
        }

        val birthday = try {
            LocalDate.parse(request.birthday)
        } catch (e: Exception) {
            throw InvalidDateFormatException()
        }

        val discriminator = generateUniqueDiscriminator(request.username)

        val hashedPassword = hashing.hash(request.password)

        val user = authRepository.createUser(request.email, request.username, hashedPassword, birthday, discriminator)
            ?: throw CreateUserFailedException()

        val token = generateToken(user.id.toString())

        return AuthResponse(token)
    }

    private suspend fun generateUniqueDiscriminator(username: String): Int {
        var discriminator: Int
        var attempts = 0
        val maxAttempts = 100

        do {
            discriminator = Random.nextInt(1000, 10000)
            val existingUser = authRepository.findByUsernameAndDiscriminator(username, discriminator)
            attempts++
        } while (existingUser != null && attempts < maxAttempts)

        if (attempts >= maxAttempts) {
            throw UniqueTagGenerationException()
        }

        return discriminator
    }

    suspend fun login(request: LoginRequest): AuthResponse {
        val user = authRepository.findByEmail(request.email)
            ?: throw InvalidCredentialsException()

        val isPasswordCorrect = hashing.verify(request.password, user.passwordHash)
        if (!isPasswordCorrect) {
            throw InvalidCredentialsException()
        }

        val token = generateToken(user.id.toString())

        return AuthResponse(token)
    }

    suspend fun getUserProfile(userId: String): UserProfileResponse {
        val uuid = try {
            UUID.fromString(userId)
        } catch (e: IllegalArgumentException) {
            throw InvalidUserIdFormatException()
        }

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
        val uuid = try {
            UUID.fromString(userId)
        } catch (e: IllegalArgumentException) {
            throw InvalidUserIdFormatException()
        }

        val currentUser = authRepository.findById(uuid)
            ?: throw UserNotFoundException()

        val newEmail = request.email?.let {
            if (it != currentUser.email) {
                if (authRepository.findByEmail(it) != null) {
                    throw EmailAlreadyExistsException()
                }
                it
            } else {
                null
            }
        }

        val newUsername = request.username?.let {
            if (it != currentUser.username) {
                val existingUserWithNewNameTag = authRepository.findByUsernameAndDiscriminator(it, currentUser.discriminator)
                if (existingUserWithNewNameTag != null) {
                    throw UsernameAlreadyTakenException(it)
                }
                it
            } else null
        }

        val newPasswordHash = request.password?.let { hashing.hash(it) }

        val newBirthday = request.birthday?.let {
            try {
                LocalDate.parse(it)
            } catch (e: Exception) {
                throw InvalidDateFormatException()
            }
        }

        val somethingToUpdate = newEmail != null || newUsername != null || newPasswordHash != null || newBirthday != null || request.avatarUrl != null

        if (somethingToUpdate) {
            val updated = authRepository.updateUser(
                userId = uuid,
                email = newEmail,
                username = newUsername,
                passwordHash = newPasswordHash,
                birthday = newBirthday,
                avatarUrl = request.avatarUrl
            )
            if (!updated) {
                throw UpdateUserFailedException()
            }
        }

        val updatedUser = authRepository.findById(uuid) ?: throw UserNotFoundException()

        return UserProfileResponse(
            id = updatedUser.id.toString(),
            email = updatedUser.email,
            username = updatedUser.username,
            discriminator = updatedUser.discriminator,
            avatarUrl = updatedUser.avatarUrl,
            birthday = updatedUser.birthday.toString()
        )
    }


    private fun generateToken(userId: String): String {
        val config = TokenConfig(
            issuer = System.getenv("JWT_ISSUER") ?: "com.intern002.locketapp",
            audience = System.getenv("JWT_AUDIENCE") ?: "users",
            expiresIn = 365L * 24L * 60L * 60L * 1000L,
            secret = System.getenv("JWT_SECRET") ?: "default-secret-for-development-only"
        )

        return tokenProvider.generateToken(
            config,
            TokenClaim("userId", userId)
        )
    }
}
