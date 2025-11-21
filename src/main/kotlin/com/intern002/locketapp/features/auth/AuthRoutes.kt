package com.intern002.locketapp.features.auth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class EmailCheckResponse(val exists: Boolean)

fun Route.authRoutes(authService: AuthService) {

    route("/auth") {

        get("/check-email") {
            val email = call.request.queryParameters["email"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing email query parameter.")

            val exists = authService.checkEmailExists(email)
            call.respond(HttpStatusCode.OK, EmailCheckResponse(exists))
        }

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

        post("/google") {
            val request = call.receive<GoogleLoginRequest>()
            when (val result = authService.handleGoogleLogin(request.idToken)) {
                is GoogleLoginResult.Success -> {
                    call.respond(HttpStatusCode.OK, result.authResponse)
                }
                is GoogleLoginResult.RegistrationRequired -> {
                    call.respond(HttpStatusCode.Accepted, result.info)
                }
            }
        }

        post("/google/complete") {
            val request = call.receive<CompleteGoogleRegistrationRequest>()
            val response = authService.completeGoogleRegistration(request)
            call.respond(HttpStatusCode.Created, response)
        }

        post("/refresh") {
            val req = call.receive<RefreshRequest>()
            val response = authService.refreshToken(req.refreshToken)
            call.respond(HttpStatusCode.OK, response)
        }
    }
}
