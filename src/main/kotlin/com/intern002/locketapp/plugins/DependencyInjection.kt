package com.intern002.locketapp.plugins

import com.intern002.locketapp.di.appModule
import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.SLF4JLogger

fun Application.configureDependencyInjection() {
    install(Koin) {
        SLF4JLogger()
        modules(appModule)
    }
}
