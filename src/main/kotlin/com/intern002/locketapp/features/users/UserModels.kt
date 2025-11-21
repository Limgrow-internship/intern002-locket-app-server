package com.intern002.locketapp.features.users

import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserRequest(
    val email: String? = null,
    val username: String? = null,
    val password: String? = null,
    val birthday: String? = null,
    val avatarUrl: String? = null
)

@Serializable
data class UserProfileResponse(
    val id: String,
    val email: String,
    val username: String,
    val discriminator: Int,
    val avatarUrl: String?,
    val birthday: String
)
