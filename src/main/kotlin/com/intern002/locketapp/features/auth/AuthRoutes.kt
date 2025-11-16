package com.intern002.locketapp.features.auth

import com.intern002.locketapp.plugins.UserIdPrincipal
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(authService: AuthService) {

    route("/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            val response = authService.register(request)
            call.respond(HttpStatusCode.Created, response)
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val response = authService.login(request)
            call.respond(HttpStatusCode.OK, response)
        }

        authenticate {
            get("/me") {
                val principal = call.principal<UserIdPrincipal>()!!
                val userProfile = authService.getUserProfile(principal.userId.toString())
                call.respond(HttpStatusCode.OK, userProfile)
            }

            put("/update") {
                val principal = call.principal<UserIdPrincipal>()!!
                val request = call.receive<UpdateUserRequest>()
                val updatedUser = authService.updateUser(principal.userId.toString(), request)
                call.respond(HttpStatusCode.OK, updatedUser)
            }

        }
    }
}
