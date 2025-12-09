package com.intern002.locketapp.features.notifications

import com.intern002.locketapp.plugins.UserIdPrincipal
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class RegisterFcmTokenRequest(
    val token: String,
    val deviceInfo: String? = null
)

@Serializable
data class UnregisterFcmTokenRequest(
    val token: String
)

fun Route.fcmTokenRoutes(fcmTokenService: FcmTokenService) {
    authenticate {
        route("/fcm") {
            post("/register") {
                val principal = call.principal<UserIdPrincipal>() ?: return@post call.respond(HttpStatusCode.Unauthorized)
                val userId = principal.userId
                val request = call.receive<RegisterFcmTokenRequest>()
                fcmTokenService.registerToken(userId, request.token, request.deviceInfo)
                call.respond(HttpStatusCode.OK, "Token registered successfully.")
            }

            post("/unregister") {
                val request = call.receive<UnregisterFcmTokenRequest>()
                val success = fcmTokenService.unregisterToken(request.token)
                if (success) {
                    call.respond(HttpStatusCode.OK, "Token unregistered successfully.")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Token not found or already inactive.")
                }
            }
        }
    }
}
