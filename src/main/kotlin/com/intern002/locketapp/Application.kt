package com.intern002.locketapp

import com.keepinwidget.plugins.configureRouting
import com.keepinwidget.plugins.configureSecurity
import com.keepinwidget.plugins.configureSerialization
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
