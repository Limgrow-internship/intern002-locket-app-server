package com.intern002.locketapp.di

import com.intern002.locketapp.core.security.JwtTokenProvider
import com.intern002.locketapp.core.security.TokenProvider
import com.intern002.locketapp.core.utils.BcryptHashing
import com.intern002.locketapp.core.utils.Hashing
import com.intern002.locketapp.features.auth.AuthRepository
import com.intern002.locketapp.features.auth.AuthRepositoryImpl
import com.intern002.locketapp.features.auth.AuthService
import org.koin.dsl.module

val appModule = module {
    // Core
    single<Hashing> { BcryptHashing() }
    single<TokenProvider> { JwtTokenProvider() }

    // Features
    single<AuthRepository> { AuthRepositoryImpl() }
    single { AuthService(get(), get(), get()) }
}
