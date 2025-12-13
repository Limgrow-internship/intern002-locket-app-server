package com.intern002.locketapp.features.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.intern002.locketapp.core.security.TokenClaim
import com.intern002.locketapp.core.security.TokenConfig
import com.intern002.locketapp.core.security.TokenProvider
import com.intern002.locketapp.core.services.EmailService
import com.intern002.locketapp.core.utils.*
import com.intern002.locketapp.features.notifications.FcmTokenRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.random.Random

class AuthService(
    private val authRepository: AuthRepository,
    private val tokenProvider: TokenProvider,
    private val hashing: Hashing,
    private val fcmTokenRepository: FcmTokenRepository,
    private val emailService: EmailService
) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    suspend fun checkEmailExists(email: String): Boolean {
        return authRepository.findByEmail(email) != null
    }

    suspend fun register(request: RegisterRequest): AuthResponse {
        if (authRepository.findByEmail(request.email) != null) throw EmailAlreadyExistsException()
        val birthday = try {
            LocalDate.parse(request.birthday)
        } catch (e: Exception) {
            throw InvalidDateFormatException()
        }
        val discriminator = generateUniqueDiscriminator(request.username)
        val passwordHash = hashing.hash(request.password)
        val user = authRepository.createUser(
            request.email,
            request.username,
            passwordHash,
            birthday,
            discriminator,
            "email",
            null
        ) ?: throw CreateUserFailedException()
        return generateAndSaveTokens(user)
    }

    suspend fun login(request: LoginRequest): AuthResponse {
        val user = authRepository.findByEmail(request.email) ?: throw InvalidCredentialsException()
        val passwordHash = user.passwordHash ?: run {
            throw InvalidCredentialsException("Use ${user.provider} to login.")
        }
        if (!hashing.verify(request.password, passwordHash)) throw InvalidCredentialsException()
        return generateAndSaveTokens(user)
    }

    suspend fun handleGoogleLogin(idToken: String): GoogleLoginResult {
        val decodedToken = verifyGoogleToken(idToken)
        authRepository.findByProviderId(decodedToken.uid)
            ?.let { return GoogleLoginResult.Success(generateAndSaveTokens(it)) }
        val email = decodedToken.email ?: throw GoogleTokenInvalidException("Email not found in token.")
        authRepository.findByEmail(email)?.let {
            authRepository.linkGoogleAccount(it.id, decodedToken.uid)
            return GoogleLoginResult.Success(generateAndSaveTokens(it))
        }
        return GoogleLoginResult.RegistrationRequired(
            GoogleRegistrationInfo(
                email = email,
                suggestedUsername = decodedToken.name ?: email.substringBefore('@')
            )
        )
    }

    suspend fun completeGoogleRegistration(request: CompleteGoogleRegistrationRequest): AuthResponse {
        val decodedToken = verifyGoogleToken(request.idToken)
        if (authRepository.findByProviderId(decodedToken.uid) != null) throw UserAlreadyExistsException()
        if (authRepository.findByEmail(decodedToken.email) != null) throw EmailAlreadyExistsException()
        val birthday = try {
            LocalDate.parse(request.birthday)
        } catch (e: Exception) {
            throw InvalidDateFormatException()
        }
        val discriminator = generateUniqueDiscriminator(request.username)
        val newUser = authRepository.createUser(
            decodedToken.email,
            request.username,
            null,
            birthday,
            discriminator,
            "google",
            decodedToken.uid
        ) ?: throw CreateUserFailedException()
        return generateAndSaveTokens(newUser)
    }

    suspend fun refreshToken(oldRefreshToken: String): AuthResponse {
        val user = authRepository.findUserByRefreshToken(oldRefreshToken)
            ?: throw InvalidCredentialsException("Invalid refresh token.")
        val newAccessToken = generateAccessToken(user.id.toString())
        val newRefreshToken = generateRefreshToken()
        authRepository.updateRefreshToken(user.id, newRefreshToken)
        return AuthResponse(newAccessToken, newRefreshToken)
    }

    suspend fun logout(userId: UUID) {
        authRepository.updateRefreshToken(userId, null)
        fcmTokenRepository.deactivateAllTokensForUser(userId)
    }

    private suspend fun generateAndSaveTokens(user: User): AuthResponse {
        val accessToken = generateAccessToken(user.id.toString())
        val refreshToken = generateRefreshToken()
        authRepository.updateRefreshToken(user.id, refreshToken)
        return AuthResponse(accessToken, refreshToken)
    }

    private fun verifyGoogleToken(idToken: String): com.google.firebase.auth.FirebaseToken {
        return try {
            FirebaseAuth.getInstance().verifyIdToken(idToken)
        } catch (e: FirebaseAuthException) {
            logger.error("Firebase token verification failed: ${e.message}", e)
            throw GoogleTokenInvalidException()
        }
    }

    private suspend fun generateUniqueDiscriminator(username: String): Int {
        var disc: Int
        var attempts = 0
        do {
            disc = Random.nextInt(1000, 9999)
            attempts++
        } while (authRepository.findByUsernameAndDiscriminator(username, disc) != null && attempts < 100)
        if (attempts >= 100) throw UniqueTagGenerationException()
        return disc
    }

    private fun generateAccessToken(userId: String): String {
        val config = TokenConfig(
            issuer = System.getenv("JWT_ISSUER") ?: "com.intern002.locketapp",
            audience = System.getenv("JWT_AUDIENCE") ?: "users",
            expiresIn = 40 * 60 * 1000L,
            secret = System.getenv("JWT_SECRET") ?: "default-secret-for-development-only"
        )
        return tokenProvider.generateToken(config, TokenClaim("userId", userId))
    }

    private fun generateRefreshToken(): String {
        val timestamp = System.currentTimeMillis()
        val randomPart = UUID.randomUUID().toString() + UUID.randomUUID().toString()
        return "$timestamp:$randomPart"
    }

    suspend fun forgotPassword(email: String) {
        if (!checkEmailExists(email)) return

        val code = (100000..999999).random().toString()
        OtpStore.saveOtp(email, code)

        withContext(Dispatchers.IO) {
            emailService.sendResetPasswordEmail(email, code)
        }
        println("📧 [OTP] Gửi đến $email: $code (Hết hạn sau 5p)")
    }

    suspend fun resetPassword(request: ResetPasswordRequest) {
        val isValid = OtpStore.verifyOtp(request.email, request.otp)
        if (!isValid) {
            throw IllegalArgumentException("Invalid or expired code.")
        }
        val newHash = hashing.hash(request.newPassword)
        authRepository.updatePassword(request.email, newHash)
    }

    suspend fun verifyCodeOnly(email: String, code: String) {
        val isValid = OtpStore.verifyOtp(email, code)
        if (!isValid) throw IllegalArgumentException("Invalid or expired code")
    }
}
