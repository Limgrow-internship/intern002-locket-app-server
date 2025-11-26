package com.intern002.locketapp.features.posts

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.postRouting() {

    val postService: PostService by inject()

    authenticate {
        route("/posts") {

            post {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("uid")?.asString()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, "Invalid Token")

                val request = call.receive<CreatePostRequest>()

                try {
                    val response = postService.createPost(userId, request)
                    call.respond(HttpStatusCode.Created, response)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid Request")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error creating post")
                }
            }
        }
    }
}