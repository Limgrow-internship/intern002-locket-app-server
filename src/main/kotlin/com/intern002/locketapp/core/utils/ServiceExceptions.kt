package com.intern002.locketapp.core.utils

import io.ktor.http.*

sealed class ServiceException(val httpStatusCode: HttpStatusCode, message: String) : Exception(message)

// Lỗi liên quan đến xác thực & quyền truy cập (401, 403, 404)
class InvalidCredentialsException : ServiceException(HttpStatusCode.Unauthorized, "Invalid email or password.")
class UserNotFoundException : ServiceException(HttpStatusCode.NotFound, "User not found.")

// Lỗi liên quan đến dữ liệu không hợp lệ từ client (400)
class EmailAlreadyExistsException : ServiceException(HttpStatusCode.BadRequest, "A user with this email already exists.")
class UsernameAlreadyTakenException(username: String) : ServiceException(HttpStatusCode.BadRequest, "Username '$username' is already taken.")
class InvalidDateFormatException : ServiceException(HttpStatusCode.BadRequest, "Invalid date format. Please use YYYY-MM-DD.")
class InvalidBodyException(val details: String) : ServiceException(HttpStatusCode.BadRequest, "Invalid request body: $details")
class InvalidUserIdFormatException : ServiceException(HttpStatusCode.BadRequest, "Invalid user ID format.")

// Lỗi server nội bộ (500)
class CreateUserFailedException : ServiceException(HttpStatusCode.InternalServerError, "Failed to create user.")
class UpdateUserFailedException : ServiceException(HttpStatusCode.InternalServerError, "Failed to update user.")
class UniqueTagGenerationException : ServiceException(HttpStatusCode.InternalServerError, "Could not generate a unique user tag.")
