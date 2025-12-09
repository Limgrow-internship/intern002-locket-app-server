package com.intern002.locketapp.plugins

import com.intern002.locketapp.features.auth.AuthService
import com.intern002.locketapp.features.auth.authRoutes
import com.intern002.locketapp.features.chat.ChatService
import com.intern002.locketapp.features.chat.chatRoutes
import com.intern002.locketapp.features.friends.FriendshipService
import com.intern002.locketapp.features.friends.friendshipRoutes
import com.intern002.locketapp.features.notifications.FcmTokenService
import com.intern002.locketapp.features.notifications.NotificationService
import com.intern002.locketapp.features.notifications.fcmTokenRoutes
import com.intern002.locketapp.features.notifications.notificationRoutes
import com.intern002.locketapp.features.posts.PostService
import com.intern002.locketapp.features.posts.postRoutes
import com.intern002.locketapp.features.reaction.ReactionService
import com.intern002.locketapp.features.reaction.reactionRoutes
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
    val friendshipService: FriendshipService by inject()
    val chatService: ChatService by inject()
    val postService: PostService by inject()
    val reactionService: ReactionService by inject()
    val fcmTokenService: FcmTokenService by inject()
    val notificationService: NotificationService by inject()

    routing {
        get("/") {
            call.respondText("Hello Locket App!")
        }

        authRoutes(authService)
        userRoutes(userService, authService)

        authenticate {
            friendshipRoutes(friendshipService)
            postRoutes(postService)
            chatRoutes(chatService)
            reactionRoutes(reactionService)
            fcmTokenRoutes(fcmTokenService)
            notificationRoutes(notificationService)

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
