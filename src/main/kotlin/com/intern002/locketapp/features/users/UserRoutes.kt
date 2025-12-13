package com.intern002.locketapp.features.users

import com.intern002.locketapp.features.auth.AuthService
import com.intern002.locketapp.plugins.UserIdPrincipal
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*

fun Route.userRoutes(userService: UserService, authService: AuthService) {

    authenticate {
        get("/users/me") {
            val principal = call.principal<UserIdPrincipal>()!!
            val profile = userService.getProfile(principal.userId.toString())
            call.respond(profile)
        }

        put("/users/update") {
            val principal = call.principal<UserIdPrincipal>()!!
            val request = call.receive<UpdateUserRequest>()
            val updated = userService.updateUser(principal.userId.toString(), request)
            call.respond(HttpStatusCode.OK, updated)
        }

        delete("/users/avatar") {
            val principal = call.principal<UserIdPrincipal>()!!
            val success = userService.deleteAvatar(principal.userId.toString())
            if (success) {
                call.respond(HttpStatusCode.OK, "Avatar deleted successfully.")
            } else {
                call.respond(HttpStatusCode.NotFound, "User not found or avatar could not be deleted.")
            }
        }

        post("/users/verify-password") {
            val principal = call.principal<UserIdPrincipal>()!!
            val request = call.receive<VerifyPasswordRequest>()
            val response = userService.verifyPassword(principal.userId.toString(), request)
            call.respond(HttpStatusCode.OK, response)
        }

        post("/users/logout") {
            val principal = call.principal<UserIdPrincipal>()!!
            authService.logout(principal.userId)
            call.respond(HttpStatusCode.OK)
        }
    }
}
