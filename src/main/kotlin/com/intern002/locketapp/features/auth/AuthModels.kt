package com.intern002.locketapp.features.auth

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val username: String,
    val password: String,
    val birthday: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RefreshRequest(
    val refreshToken: String
)

@Serializable
data class GoogleLoginRequest(val idToken: String)

@Serializable
data class CompleteGoogleRegistrationRequest(
    val idToken: String,
    val username: String,
    val birthday: String
)

// --- Response Models ---

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class GoogleRegistrationInfo(val email: String, val suggestedUsername: String)



sealed class GoogleLoginResult {
    data class Success(val authResponse: AuthResponse) : GoogleLoginResult()
    data class RegistrationRequired(val info: GoogleRegistrationInfo) : GoogleLoginResult()
}
