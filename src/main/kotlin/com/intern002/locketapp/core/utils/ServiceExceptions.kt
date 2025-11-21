package com.intern002.locketapp.core.utils

import io.ktor.http.*

sealed class ServiceException(val httpStatusCode: HttpStatusCode, message: String) : Exception(message)

// (401, 403, 404)
class InvalidCredentialsException(message: String = "Invalid email or password.") : ServiceException(HttpStatusCode.Unauthorized, message)
class UserNotFoundException : ServiceException(HttpStatusCode.NotFound, "User not found.")
class RefreshTokenExpiredException : ServiceException(HttpStatusCode.Unauthorized, "Refresh token expired. Please login again.")
class RefreshTokenMissingException(message: String = "Refresh token is missing or malformed.") : ServiceException(HttpStatusCode.Unauthorized, message)

// (400)
class EmailAlreadyExistsException : ServiceException(HttpStatusCode.BadRequest, "A user with this email already exists.")
class UserAlreadyExistsException : ServiceException(HttpStatusCode.BadRequest, "This user has already been created.")
class UsernameAlreadyTakenException(username: String) : ServiceException(HttpStatusCode.BadRequest, "Username '$username' is already taken.")
class InvalidDateFormatException : ServiceException(HttpStatusCode.BadRequest, "Invalid date format. Please use YYYY-MM-DD.")
class InvalidBodyException(val details: String) : ServiceException(HttpStatusCode.BadRequest, "Invalid request body: $details")
class InvalidUserIdFormatException : ServiceException(HttpStatusCode.BadRequest, "Invalid user ID format.")

// (500)
class CreateUserFailedException : ServiceException(HttpStatusCode.InternalServerError, "Failed to create user.")
class UpdateUserFailedException : ServiceException(HttpStatusCode.InternalServerError, "Failed to update user.")
class UniqueTagGenerationException : ServiceException(HttpStatusCode.InternalServerError, "Could not generate a unique user tag.")
class AccountLinkFailedException : ServiceException(HttpStatusCode.InternalServerError, "Failed to link social account.")

// Google/Firebase
class GoogleTokenInvalidException(message: String = "Invalid Google ID token.") : ServiceException(HttpStatusCode.Unauthorized, message)
