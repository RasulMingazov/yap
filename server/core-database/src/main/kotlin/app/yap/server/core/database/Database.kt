package app.yap.server.core.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("app.yap.server.core.database")

fun openDatabase(config: DatabaseConfig): AutoCloseable {
    logger.info("Connecting to PostgreSQL at {}", config.url.substringAfter("//").substringBefore('?'))
    val source = HikariDataSource(config.toHikariConfig())
    runCatching {
        DatabaseMigrations.migrate(
            resetOnMigrationChange = config.resetOnMigrationChange,
            source = source,
        )
        Database.connect(source)
    }
        .onFailure { source.close() }
        .getOrThrow()
    return source
}

private fun DatabaseConfig.toHikariConfig(): HikariConfig = HikariConfig().apply {
    jdbcUrl = url
    username = user
    password = this@toHikariConfig.password
    driverClassName = "org.postgresql.Driver"
    maximumPoolSize = poolSize
    minimumIdle = 1
    connectionTimeout = CONNECTION_TIMEOUT_MILLIS
    isAutoCommit = false
    transactionIsolation = "TRANSACTION_READ_COMMITTED"
    poolName = "yap-db"
}

private const val CONNECTION_TIMEOUT_MILLIS = 5_000L
