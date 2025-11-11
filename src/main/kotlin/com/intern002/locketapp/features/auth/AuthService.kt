package com.intern002.locketapp.features.auth

import com.intern002.locketapp.core.security.TokenClaim
import com.intern002.locketapp.core.security.TokenConfig
import com.intern002.locketapp.core.security.TokenProvider
import com.intern002.locketapp.core.utils.Hashing

class UserAlreadyExistsException : Exception("User with this email already exists")

class AuthService(
    val authRepository: AuthRepository,
    val tokenProvider: TokenProvider,
    val hashing: Hashing,
    val tokenConfig: TokenConfig
) {
    suspend fun registerUser(request: AuthRequest): AuthResponse {
        val existingUser = authRepository.findUserByEmail(request.email)
        if (existingUser != null) {
            throw UserAlreadyExistsException()
        }
        val passwordHash = hashing.hash(request.password)

        val newUser = authRepository.createUser(request, passwordHash)
            ?: throw Exception("Fail to create user")

        val token = tokenProvider.generateToken(
            config = tokenConfig,
            TokenClaim(name = "uid", value = newUser.id.toString())
        )

        return AuthResponse(
            token = token,
            user = UserResponse(
                id = newUser.id.toString(),
                email = newUser.email,
                username = newUser.username,
                discriminator = newUser.discriminator,
                birthday = newUser.birthday.toString()
            )
        )
    }
}