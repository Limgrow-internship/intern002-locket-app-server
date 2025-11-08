package com.intern002.locketapp.core.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

data class TokenClaim(
    val name: String,
    val value: String
)

data class TokenConfig(
    val issuer: String,
    val audience: String,
    val expiresIn: Long,
    val secret: String
)

interface TokenProvider {
    fun generateToken(config: TokenConfig, vararg claims: TokenClaim): String
}

class JwtTokenProvider : TokenProvider {
    override fun generateToken(config: TokenConfig, vararg claims: TokenClaim): String {
        val token = JWT.create()
            .withAudience(config.audience)
            .withIssuer(config.issuer)
            .withExpiresAt(Date(System.currentTimeMillis() + config.expiresIn))

        claims.forEach { claim ->
            token.withClaim(claim.name, claim.value)
        }

        return token.sign(Algorithm.HMAC256(config.secret))
    }
}
