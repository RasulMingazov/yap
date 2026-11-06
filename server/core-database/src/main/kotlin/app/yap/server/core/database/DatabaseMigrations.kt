package app.yap.server.core.database

import javax.sql.DataSource
import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory

internal object DatabaseMigrations {

    private const val APPLICATION_LOCATION = "classpath:db/migration"
    private val logger = LoggerFactory.getLogger(DatabaseMigrations::class.java)

    fun migrate(
        resetOnMigrationChange: Boolean,
        source: DataSource,
        location: String = APPLICATION_LOCATION,
    ) {
        val flyway = Flyway.configure()
            .dataSource(source)
            .locations(location)
            .validateMigrationNaming(true)
            .ignoreMigrationPatterns("*:pending")
            .cleanDisabled(!resetOnMigrationChange)
            .load()
        if (resetOnMigrationChange && !flyway.validateWithResult().validationSuccessful) {
            logger.warn("Applied migrations changed: clearing the database and migrating from scratch")
            flyway.clean()
        }
        flyway.migrate()
    }
}
