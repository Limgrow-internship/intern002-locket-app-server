package com.intern002.locketapp.di

import com.intern002.locketapp.core.security.JwtTokenProvider
import com.intern002.locketapp.core.security.TokenConfig
import com.intern002.locketapp.core.security.TokenProvider
import com.intern002.locketapp.core.utils.BcryptHashing
import com.intern002.locketapp.core.utils.Hashing
import com.intern002.locketapp.features.auth.AuthRepository
import com.intern002.locketapp.features.auth.AuthService
import io.ktor.server.application.Application
import org.koin.dsl.module

val appModule = module {
    single<Hashing> { BcryptHashing() }
    single<TokenProvider> { JwtTokenProvider() }

    single { AuthRepository() }

    single {
        val config = get<Application>().environment.config
        TokenConfig(
            issuer = config.property("jwt.domain").getString(),
            audience = config.property("jwt.audience").getString(),
            expiresIn = config.property("expiresIn").getString().toLong(),
            secret = config.property("jwt.secret").getString(),
        )
    }
    single {
        AuthService(
            authRepository = get(),
            tokenProvider = get(),
            hashing = get(),
            tokenConfig = get()
        )
    }
}
