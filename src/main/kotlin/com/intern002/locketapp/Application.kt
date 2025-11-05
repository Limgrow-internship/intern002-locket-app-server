package com.intern002.locketapp

import com.intern002.locketapp.plugins.configureRouting
import com.intern002.locketapp.plugins.configureSecurity
import com.intern002.locketapp.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureSecurity()
    configureSerialization()
    configureRouting()
}
