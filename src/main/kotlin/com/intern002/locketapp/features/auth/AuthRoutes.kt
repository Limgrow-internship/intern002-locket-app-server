package com.intern002.locketapp.features.auth

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.authRouting() {
    val authService: AuthService by inject()

    route("/auth"){

        post("/register") {
            try{
                val request = call.receive<AuthRequest>()
                val response = authService.registerUser(request)
                call.respond(HttpStatusCode.Created, response)

            } catch(e: UserAlreadyExistsException) {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }
    }
}