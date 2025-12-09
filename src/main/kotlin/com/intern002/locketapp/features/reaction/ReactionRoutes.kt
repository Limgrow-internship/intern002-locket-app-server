package com.intern002.locketapp.features.reaction

import com.intern002.locketapp.plugins.UserIdPrincipal
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.reactionRoutes(reactionService: ReactionService) {

    get("/meta/reactions") {
        val reactions = reactionService.getAllReactionTypes()
        call.respond(HttpStatusCode.OK, reactions)
    }

    authenticate {
        post("/posts/react") {
            val principal = call.principal<UserIdPrincipal>()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val request = call.receive<ReactToPostRequest>()

            try {
                reactionService.reactToPost(principal.userId, request)
                call.respond(HttpStatusCode.OK, mapOf("message" to "Reacted successfully"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Error")
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Failed to react")
            }
        }
    }
}