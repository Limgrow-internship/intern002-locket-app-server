package com.intern002.locketapp.features.notifications

import com.intern002.locketapp.plugins.UserIdPrincipal
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.notificationRoutes(notificationService: NotificationService) {
    authenticate {
        route("/notifications") {
            get {
                val principal = call.principal<UserIdPrincipal>() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val notifications = notificationService.getNotifications(principal.userId)
                call.respond(HttpStatusCode.OK, notifications)
            }

            put("/read-all") {
                val principal = call.principal<UserIdPrincipal>() ?: return@put call.respond(HttpStatusCode.Unauthorized)
                val success = notificationService.markAllAsRead(principal.userId)
                if (success) {
                    call.respond(HttpStatusCode.OK, "All notifications marked as read.")
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to mark all notifications as read.")
                }
            }

            put("/{id}/read") {
                val principal = call.principal<UserIdPrincipal>() ?: return@put call.respond(HttpStatusCode.Unauthorized)
                val notificationId = call.parameters["id"]?.let { UUID.fromString(it) } ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid notification ID")

                // Optional: Check if the notification actually belongs to the user before marking as read
                // For now, we'll trust the client.

                val success = notificationService.markNotificationAsRead(notificationId)
                if (success) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Notification not found.")
                }
            }
        }
    }
}
