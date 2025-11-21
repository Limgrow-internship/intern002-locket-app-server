package com.intern002.locketapp.plugins

import com.intern002.locketapp.core.utils.ServiceException
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<ServiceException> { call, cause ->
            call.respond(cause.httpStatusCode, cause.message ?: "An unknown error occurred.")
        }
    }
}
