package com.intern002.locketapp.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.util.UUID

data class UserIdPrincipal(val userId: UUID)

fun Application.configureSecurity() {
    val jwtSecret = System.getenv("JWT_SECRET") ?: "default-secret-for-development-only"
    val jwtAudience = System.getenv("JWT_AUDIENCE") ?: "users"
    val jwtIssuer = System.getenv("JWT_ISSUER") ?: "com.intern002.locketapp"
    val jwtRealm = "Locket App"

    install(Authentication) {
        jwt {
            realm = jwtRealm
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtIssuer)
                    .build()
            )
            validate { credential ->
                val userIdString = credential.payload.getClaim("userId").asString()

                if (userIdString != null) {
                    try {
                        UserIdPrincipal(UUID.fromString(userIdString))
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                } else {
                    null
                }
            }
        }
    }
}
