package app.yap.server.core.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.slf4j.LoggerFactory
import org.jetbrains.exposed.v1.jdbc.Database as ExposedDatabase

private val logger = LoggerFactory.getLogger("app.yap.server.core.database")

class Database internal constructor(
    private val source: HikariDataSource,
) : AutoCloseable {

    private val exposedDatabase = ExposedDatabase.connect(source)

    suspend fun <T> transaction(statement: suspend JdbcTransaction.() -> T): T = withContext(Dispatchers.IO) {
        suspendTransaction(db = exposedDatabase, statement = statement)
    }

    override fun close() {
        source.close()
    }
}

fun openDatabase(config: DatabaseConfig): Database {
    logger.info("Connecting to PostgreSQL at {}", config.url.substringAfter("//").substringBefore('?'))
    val source = HikariDataSource(config.toHikariConfig())
    runCatching {
        DatabaseMigrations.migrate(
            resetOnMigrationChange = config.resetOnMigrationChange,
            source = source,
        )
    }
        .onFailure { source.close() }
        .getOrThrow()
    return Database(source)
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
