package com.intern002.locketapp.features.chat

import com.intern002.locketapp.plugins.UserIdPrincipal
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.chatRoutes(chatService: ChatService) {
    route("/chat") {
        get("/conversations") {
            val principal = call.principal<UserIdPrincipal>() ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val conversations = chatService.getConversations(principal.userId)
            call.respond(HttpStatusCode.OK, conversations)
        }

        post("/messages") {
            val principal = call.principal<UserIdPrincipal>() ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val request = call.receive<SendMessageRequest>()
            val newMessage = chatService.saveMessage(principal.userId, request)
            call.respond(HttpStatusCode.Created, newMessage)
        }

        get("/messages/{conversationId}") {
            val principal = call.principal<UserIdPrincipal>() ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val conversationId = call.parameters["conversationId"]?.let { UUID.fromString(it) } ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid conversation ID")

            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20

            val messages = chatService.getMessages(principal.userId, conversationId, page, pageSize)
            call.respond(HttpStatusCode.OK, messages)
        }
    }
}