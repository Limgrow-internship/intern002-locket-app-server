package com.intern002.locketapp.di

import com.intern002.locketapp.core.security.JwtTokenProvider
import com.intern002.locketapp.core.security.TokenProvider
import com.intern002.locketapp.core.utils.BcryptHashing
import com.intern002.locketapp.core.utils.Hashing
import com.intern002.locketapp.features.auth.AuthRepository
import com.intern002.locketapp.features.auth.AuthRepositoryImpl
import com.intern002.locketapp.features.auth.AuthService
import com.intern002.locketapp.features.chat.ChatRepository
import com.intern002.locketapp.features.chat.ChatRepositoryImpl
import com.intern002.locketapp.features.chat.ChatService
import com.intern002.locketapp.features.friends.FriendshipRepository
import com.intern002.locketapp.features.friends.FriendshipRepositoryImpl
import com.intern002.locketapp.features.friends.FriendshipService
import com.intern002.locketapp.features.posts.PostRepository
import com.intern002.locketapp.features.posts.PostRepositoryImpl
import com.intern002.locketapp.features.posts.PostService
import com.intern002.locketapp.features.users.UserRepository
import com.intern002.locketapp.features.users.UserRepositoryImpl
import com.intern002.locketapp.features.users.UserService
import org.koin.dsl.module

val appModule = module {
    single<Hashing> { BcryptHashing() }
    single<TokenProvider> { JwtTokenProvider() }

    single<AuthRepository> { AuthRepositoryImpl() }
    single { AuthService(get(), get(), get()) }

    single<UserRepository> { UserRepositoryImpl() }
    single { UserService(get(), get(), get()) }

    single<FriendshipRepository> { FriendshipRepositoryImpl() }
    single { FriendshipService(get()) } 

    single<ChatRepository> { ChatRepositoryImpl() }
    single { ChatService(get()) }

    single<PostRepository> { PostRepositoryImpl() }
    single { PostService(get()) }
}
