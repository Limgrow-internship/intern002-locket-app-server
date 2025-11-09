package com.intern002.locketapp.features.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(
    val email: String,
    val password: String,
    val username: String,
    val discriminator: Int,
    val birthday: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserResponse
)

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val username: String,
    val discriminator: Int,
    val birthday: String
)