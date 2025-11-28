package com.intern002.locketapp.features.posts

import com.intern002.locketapp.plugins.UserIdPrincipal
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*


fun Route.postRoutes(postService: PostService) {

    route("/posts") {

        post {
            val principal = call.principal<UserIdPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val userIdString = principal.userId.toString()

            if (userIdString.isBlank()) {
                return@post call.respond(HttpStatusCode.Unauthorized, "Token is missing userId claim")
            }

            val userId = try {
                UUID.fromString(userIdString)
            } catch (e: Exception) {
                return@post call.respond(HttpStatusCode.BadRequest, "Invalid User ID format in Token")
            }

            try {
                val request = call.receive<CreatePostRequest>()
                val response = postService.createPost(userId, request)
                call.respond(HttpStatusCode.Created, response)

            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Failed to create post")
            }
        }
    }
}