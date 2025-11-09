package com.intern002.locketapp.di

import com.intern002.locketapp.core.security.JwtTokenProvider
import com.intern002.locketapp.core.security.TokenProvider
import com.intern002.locketapp.core.utils.BcryptHashing
import com.intern002.locketapp.core.utils.Hashing
import com.intern002.locketapp.features.auth.AuthRepository
import com.intern002.locketapp.features.auth.AuthService
import org.koin.dsl.module

val appModule = module {
    single<Hashing> { BcryptHashing() }
    single<TokenProvider> { JwtTokenProvider() }

    single { AuthRepository() }

    single {
        AuthService(
            authRepository = get(),
            tokenProvider = get(),
            hashing = get()
        )
    }
}
