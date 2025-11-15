package com.thoni

import com.thoni.config.AppConfig
import com.thoni.config.AppConfigLoader
import com.thoni.database.DatabaseFactory
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import kotlinx.serialization.Serializable
import org.slf4j.event.Level

private val AppConfigKey = AttributeKey<AppConfig>("AppConfig")

/**
 * Entry point used by the Gradle application plugin and Docker runtime.
 */
fun main(args: Array<String>) = EngineMain.main(args)

/**
 * Application module wires configuration, persistence, and HTTP plugins together.
 */
@Suppress("unused")
fun Application.module() {
    val appConfig = AppConfigLoader.load(environment.config)
    attributes.put(AppConfigKey, appConfig)

    DatabaseFactory.connect(appConfig.database)

    install(CallLogging) {
        level = Level.INFO
    }
    install(ContentNegotiation) {
        json()
    }

    routing {
        get("/health") {
            call.respond(
                HealthResponse(
                    status = "ok",
                    environment = appConfig.env,
                ),
            )
        }
    }
}

fun Application.requireAppConfig(): AppConfig = attributes[AppConfigKey]

@Serializable
data class HealthResponse(
    val status: String,
    val environment: String,
)
