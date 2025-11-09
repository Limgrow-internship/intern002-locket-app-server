package com.intern002.locketapp.features.auth

import com.intern002.locketapp.core.security.TokenProvider
import com.intern002.locketapp.core.utils.Hashing

class AuthService(
    val authRepository: AuthRepository,
    val tokenProvider: TokenProvider,
    val hashing: Hashing
) {

}