package com.thoni.database

import com.thoni.config.DatabaseConfig
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection

object DatabaseFactory {
    @Volatile
    private var database: Database? = null

    fun connect(config: DatabaseConfig): Database {
        return database ?: createDatabase(config).also { database = it }
    }

    private fun createDatabase(config: DatabaseConfig): Database {
        maybePrepareSqliteFile(config.url)

        val db = Database.connect(
            url = config.url,
            driver = config.driver,
            setupConnection = { connection ->
                configureConnection(connection, config)
            },
        )

        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE

        return db
    }

    private fun maybePrepareSqliteFile(jdbcUrl: String) {
        val prefix = "jdbc:sqlite:"
        if (!jdbcUrl.startsWith(prefix) || jdbcUrl == "jdbc:sqlite::memory:") {
            return
        }
        val filePath = jdbcUrl.removePrefix(prefix)
        val path = Path.of(filePath)
        val directory = path.parent ?: return
        if (!Files.exists(directory)) {
            Files.createDirectories(directory)
        }
    }

    private fun configureConnection(connection: Connection, config: DatabaseConfig) {
        if (!config.driver.contains("sqlite", ignoreCase = true)) {
            return
        }
        connection.createStatement().use { statement ->
            statement.execute("PRAGMA foreign_keys = ON;")
            statement.execute("PRAGMA journal_mode = WAL;")
        }
    }
}
