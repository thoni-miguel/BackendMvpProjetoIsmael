package com.thoni

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.slf4j.event.Level

/**
 * Entry point used by the Gradle application plugin and Docker runtime.
 */
fun main(args: Array<String>) = EngineMain.main(args)

/**
 * Minimal application module to verify wiring and configuration loading.
 */
@Suppress("unused")
fun Application.module() {
    install(CallLogging) {
        level = Level.INFO
    }
    install(ContentNegotiation) {
        json()
    }

    routing {
        get("/health") {
            val env = this@module.environment.config
                .propertyOrNull("app.env")
                ?.getString()
                .orEmpty()
            call.respond(HealthResponse(status = "ok", environment = env.ifBlank { "local" }))
        }
    }
}

@Serializable
data class HealthResponse(
    val status: String,
    val environment: String,
)
