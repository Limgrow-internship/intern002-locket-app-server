package com.intern002.locketapp.plugins

import com.intern002.locketapp.features.auth.AuthService
import com.intern002.locketapp.features.auth.authRoutes
import com.intern002.locketapp.features.friends.FriendshipService
import com.intern002.locketapp.features.friends.friendshipRoutes
import com.intern002.locketapp.features.posts.postRouting
import com.intern002.locketapp.features.users.UserService
import com.intern002.locketapp.features.users.userRoutes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {

    val authService: AuthService by inject()
    val userService: UserService by inject()
    val friendshipService: FriendshipService by inject() // Use FriendshipService

    routing {
        get("/") {
            call.respondText("Hello Locket App!")
        }

        authRoutes(authService)
        userRoutes(userService, authService)
        postRouting()

        authenticate {
            // All friendship routes require a user to be logged in.
            friendshipRoutes(friendshipService) // Pass the service

            get("/test/me") {
                val principal = call.principal<UserIdPrincipal>()

                val userId = principal?.userId

                if (userId != null) {
                    call.respondText("Authentication successful. Your user ID is: $userId")
                } else {
                    call.respond(HttpStatusCode.Unauthorized)
                }
            }
        }
    }
}
