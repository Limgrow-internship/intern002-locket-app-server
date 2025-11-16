package com.intern002.locketapp.plugins

import com.intern002.locketapp.features.auth.AuthService
import com.intern002.locketapp.features.auth.authRoutes
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {

    val authService: AuthService by inject()

    routing {
        get("/") {
            call.respondText("Hello Locket App!")
        }

        authRoutes(authService)

        authenticate {
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
