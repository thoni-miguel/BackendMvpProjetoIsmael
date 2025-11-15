package com.thoni.config

import io.ktor.server.config.*

/**
 * Strongly typed representation of application settings loaded from HOCON.
 */
data class AppConfig(
    val env: String,
    val database: DatabaseConfig,
    val auth: AuthConfig,
    val storage: StorageConfig,
    val rateLimit: RateLimitConfig,
)

data class DatabaseConfig(
    val driver: String,
    val url: String,
)

data class AuthConfig(
    val apiKey: String,
)

data class StorageConfig(
    val mediaRoot: String,
)

data class RateLimitConfig(
    val requestsPerMinute: Int,
)

/**
 * Loads [AppConfig] from a [ApplicationConfig] instance.
 */
object AppConfigLoader {
    fun load(config: ApplicationConfig): AppConfig {
        return AppConfig(
            env = config.string("app.env", default = "local"),
            database = DatabaseConfig(
                driver = config.string("app.database.driver"),
                url = config.string("app.database.url"),
            ),
            auth = AuthConfig(
                apiKey = config.string("app.auth.api_key"),
            ),
            storage = StorageConfig(
                mediaRoot = config.string("app.storage.media_root"),
            ),
            rateLimit = RateLimitConfig(
                requestsPerMinute = config.int("app.rate_limit.requests_per_minute", default = 60),
            ),
        )
    }
}

private fun ApplicationConfig.string(path: String, default: String? = null): String {
    return propertyOrNull(path)?.getString() ?: default ?: error("Missing config property: $path")
}

private fun ApplicationConfig.int(path: String, default: Int? = null): Int {
    return propertyOrNull(path)?.getString()?.toInt()
        ?: default
        ?: error("Missing config property: $path")
}
