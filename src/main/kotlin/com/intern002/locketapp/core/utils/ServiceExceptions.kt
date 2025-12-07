package com.intern002.locketapp.core.utils

import io.ktor.http.*

sealed class ServiceException(val httpStatusCode: HttpStatusCode, message: String) : Exception(message)

// (401, 403, 404)
class InvalidCredentialsException(message: String = "Incorrect email or password.") : ServiceException(HttpStatusCode.Unauthorized, message)
class UserNotFoundException : ServiceException(HttpStatusCode.NotFound, "User not found.")
class NotMemberOfConversationException(message: String = "You are not a member of this conversation.") : ServiceException(HttpStatusCode.Forbidden, message)
class RefreshTokenExpiredException : ServiceException(HttpStatusCode.Unauthorized, "Refresh token expired. Please login again.")
class RefreshTokenMissingException(message: String = "Refresh token is missing or malformed.") : ServiceException(HttpStatusCode.Unauthorized, message)
class WrongPasswordException: ServiceException(HttpStatusCode.Unauthorized, "The password you entered is incorrect.")

// (409 - Conflict)
class EmailAlreadyExistsException : ServiceException(HttpStatusCode.Conflict, "A user with this email already exists.")
class UserAlreadyExistsException : ServiceException(HttpStatusCode.Conflict, "This user has already been created.")
class UsernameAlreadyTakenException(username: String) : ServiceException(HttpStatusCode.Conflict, "Username '$username' is already taken.")

// (400 - Bad Request)
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
