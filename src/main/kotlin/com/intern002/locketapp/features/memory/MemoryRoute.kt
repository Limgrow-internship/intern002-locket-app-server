package com.intern002.locketapp.features.memory

import com.intern002.locketapp.plugins.UserIdPrincipal
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.memoryRoutes(memoryService: MemoryService) {

    authenticate {
        route("/memories") {

            // GET /memories?month=12&year=2024
            get {
                val principal = call.principal<UserIdPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)

                val userId = principal.userId

                val month = call.request.queryParameters["month"]?.toIntOrNull()
                val year = call.request.queryParameters["year"]?.toIntOrNull()

                if (month == null || year == null) {
                    return@get call.respond(HttpStatusCode.BadRequest, "Missing month or year")
                }

                try {
                    val memories = memoryService.getMemories(userId, month, year)
                    call.respond(HttpStatusCode.OK, memories)
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, "Failed to get memories")
                }
            }

            post {
                val principal = call.principal<UserIdPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)

                val userId = principal.userId

                try {
                    val request = call.receive<CreateMemoryRequest>()
                    val success = memoryService.createOrUpdateMemory(userId, request)

                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf("message" to "Saved"))
                    } else {
                        call.respond(HttpStatusCode.InternalServerError, "Failed to save memory")
                    }

                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid data")
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, "Failed to create/update memory")
                }
            }
        }
    }
}