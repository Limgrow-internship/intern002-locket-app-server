package com.intern002.locketapp.di

import com.intern002.locketapp.core.security.JwtTokenProvider
import com.intern002.locketapp.core.security.TokenProvider
import com.intern002.locketapp.core.utils.BcryptHashing
import com.intern002.locketapp.core.utils.Hashing
import org.koin.dsl.module

val appModule = module {
    single<Hashing> { BcryptHashing() }
    single<TokenProvider> { JwtTokenProvider() }
}
