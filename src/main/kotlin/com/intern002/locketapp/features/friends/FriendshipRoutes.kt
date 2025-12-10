package com.intern002.locketapp.features.friends

import com.intern002.locketapp.plugins.UserIdPrincipal
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class FriendRequest(val username: String, val discriminator: Int)

@Serializable
data class GenericResponse(val success: Boolean, val message: String)

@Serializable
data class FriendUserResponse(
    val id: String,
    val username: String,
    val discriminator: Int,
    val avatarUrl: String?
)

@Serializable
data class SentFriendRequestResponse(
    val friendshipId: String,
    val addressee: FriendUserResponse,
    val status: String
)

@Serializable
data class FriendResponse(
    val user: FriendUserResponse,
    val status: String
)

fun Route.friendshipRoutes(friendshipService: FriendshipService) {
    route("/friends") {
        authenticate {
            get("/suggestions") {
                val principal = call.principal<UserIdPrincipal>()!!
                val userId = principal.userId

                val suggestions = friendshipService.getFriendSuggestions(userId)
                call.respond(HttpStatusCode.OK, suggestions)
            }

            get("/search") {
                val principal = call.principal<UserIdPrincipal>()!!
                val searcherId = principal.userId

                val username = call.request.queryParameters["username"]
                val discriminator = call.request.queryParameters["discriminator"]?.toIntOrNull()

                if (username.isNullOrBlank() || discriminator == null) {
                    return@get call.respond(HttpStatusCode.BadRequest, GenericResponse(false, "Username and discriminator are required."))
                }

                val foundUser = friendshipService.findUser(searcherId, username, discriminator)
                if (foundUser == null) {
                    call.respond(HttpStatusCode.NotFound, GenericResponse(false, "User not found."))
                } else {
                    call.respond(HttpStatusCode.OK, foundUser)
                }
            }

            get("/blocked") {
                val principal = call.principal<UserIdPrincipal>()!!
                val blockerId = principal.userId

                val blockedUsers = friendshipService.getBlockedUsers(blockerId)
                call.respond(HttpStatusCode.OK, blockedUsers)
            }

            post("/requests") {
                val principal = call.principal<UserIdPrincipal>()!!
                val requesterId = principal.userId

                val request = call.receive<FriendRequest>()
                val result = friendshipService.sendRequest(requesterId, request.username, request.discriminator)

                result.onSuccess {
                    call.respond(HttpStatusCode.Created, GenericResponse(true, "Friend request sent."))
                }.onFailure {
                    val statusCode = when {
                        it.message?.contains("not found", ignoreCase = true) == true -> HttpStatusCode.NotFound
                        it.message?.contains("yourself", ignoreCase = true) == true -> HttpStatusCode.BadRequest
                        else -> HttpStatusCode.Conflict
                    }
                    call.respond(statusCode, GenericResponse(false, it.message ?: "An error occurred"))
                }
            }

            get("/requests/sent") {
                val principal = call.principal<UserIdPrincipal>()!!
                val userId = principal.userId

                val sentRequests = friendshipService.getSentRequestsForUser(userId)
                call.respond(HttpStatusCode.OK, sentRequests)
            }

            get("/requests/pending") {
                val principal = call.principal<UserIdPrincipal>()!!
                val userId = principal.userId

                val pendingRequests = friendshipService.getPendingRequestsForUser(userId)
                call.respond(HttpStatusCode.OK, pendingRequests)
            }

            put("/accept/{friendshipId}") {
                val principal = call.principal<UserIdPrincipal>()!!
                val friendshipId = call.parameters["friendshipId"]?.let { UUID.fromString(it) } ?: return@put call.respond(HttpStatusCode.BadRequest)

                val success = friendshipService.acceptRequest(friendshipId, principal.userId)
                if (success) {
                    call.respond(HttpStatusCode.OK, GenericResponse(true, "Friend request accepted."))
                } else {
                    call.respond(HttpStatusCode.Conflict, GenericResponse(false, "Failed to accept friend request. It might not be pending or doesn't exist."))
                }
            }

            put("/reject/{friendshipId}") {
                val friendshipId = call.parameters["friendshipId"]?.let { UUID.fromString(it) } ?: return@put call.respond(HttpStatusCode.BadRequest)

                val success = friendshipService.rejectRequest(friendshipId)
                if (success) {
                    call.respond(HttpStatusCode.OK, GenericResponse(true, "Friend request rejected."))
                } else {
                    call.respond(HttpStatusCode.Conflict, GenericResponse(false, "Failed to reject friend request. It might not be pending or doesn't exist."))
                }
            }

            post("/block/{friendId}") {
                val principal = call.principal<UserIdPrincipal>()!!
                val blockerId = principal.userId
                val friendId = call.parameters["friendId"]?.let { UUID.fromString(it) } ?: return@post call.respond(HttpStatusCode.BadRequest)

                val success = friendshipService.blockFriend(blockerId, friendId)
                if (success) {
                    call.respond(HttpStatusCode.OK, GenericResponse(true, "User blocked."))
                } else {
                    call.respond(HttpStatusCode.NotFound, GenericResponse(false, "Friendship not found or already blocked."))
                }
            }

            post("/unblock/{friendId}") {
                val principal = call.principal<UserIdPrincipal>()!!
                val blockerId = principal.userId
                val friendId = call.parameters["friendId"]?.let { UUID.fromString(it) } ?: return@post call.respond(HttpStatusCode.BadRequest)

                val success = friendshipService.unblockFriend(blockerId, friendId)
                if (success) {
                    call.respond(HttpStatusCode.OK, GenericResponse(true, "User unblocked and friendship restored."))
                } else {
                    call.respond(HttpStatusCode.NotFound, GenericResponse(false, "Block record not found."))
                }
            }

            delete("/{friendId}") {
                val principal = call.principal<UserIdPrincipal>()!!
                val userId = principal.userId

                val friendId = call.parameters["friendId"]?.let { UUID.fromString(it) } ?: return@delete call.respond(HttpStatusCode.BadRequest)

                val success = friendshipService.unfriend(userId, friendId)
                if (success) {
                    call.respond(HttpStatusCode.OK, GenericResponse(true, "Friend removed."))
                } else {
                    call.respond(HttpStatusCode.NotFound, GenericResponse(false, "Friendship not found."))
                }
            }

            get {
                val principal = call.principal<UserIdPrincipal>()!!
                val userId = principal.userId

                val friends = friendshipService.getFriendsForUser(userId)
                call.respond(HttpStatusCode.OK, friends)
            }
        }
    }
}