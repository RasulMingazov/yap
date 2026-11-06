package app.yap.server.core.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.flywaydb.core.api.exception.FlywayValidateException
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

internal class DatabaseMigrationsTest {

    @Test
    fun `GIVEN a changed applied migration and the reset on WHEN migrations run THEN the database is rebuilt`() {
        assumeDocker()

        withDatabase { source ->
            DatabaseMigrations.migrate(location = ORIGINAL, resetOnMigrationChange = true, source = source)

            DatabaseMigrations.migrate(location = CHANGED, resetOnMigrationChange = true, source = source)

            assertEquals(expected = listOf("body", "id") to 0, actual = source.noteColumns() to source.noteCount())
        }
    }

    @Test
    fun `GIVEN a changed applied migration and the reset off WHEN migrations run THEN they fail and the data stays`() {
        assumeDocker()

        withDatabase { source ->
            DatabaseMigrations.migrate(location = ORIGINAL, resetOnMigrationChange = false, source = source)

            assertFailsWith<FlywayValidateException> {
                DatabaseMigrations.migrate(location = CHANGED, resetOnMigrationChange = false, source = source)
            }
            assertEquals(expected = 1, actual = source.noteCount())
        }
    }

    @Test
    fun `GIVEN a new migration and the reset on WHEN migrations run THEN it is applied without clearing the data`() {
        assumeDocker()

        withDatabase { source ->
            DatabaseMigrations.migrate(location = ORIGINAL, resetOnMigrationChange = true, source = source)

            DatabaseMigrations.migrate(location = ADDED, resetOnMigrationChange = true, source = source)

            assertEquals(expected = 2, actual = source.noteCount())
        }
    }

    @Test
    fun `GIVEN an empty database WHEN the server opens it THEN the server migrations apply`() {
        withTestDatabase { }
    }

    private fun withDatabase(block: (HikariDataSource) -> Unit) {
        PostgreSQLContainer(DockerImageName.parse(POSTGRES_IMAGE)).use { container ->
            container.start()
            HikariDataSource(
                HikariConfig().apply {
                    jdbcUrl = container.jdbcUrl
                    username = container.username
                    password = container.password
                },
            ).use(block)
        }
    }

    private fun HikariDataSource.noteCount(): Int = query("select count(*) from notes").single().toInt()

    private fun HikariDataSource.noteColumns(): List<String> = query(
        "select column_name from information_schema.columns where table_name = 'notes' order by column_name",
    )

    private fun HikariDataSource.query(sql: String): List<String> = connection.use { connection ->
        connection.createStatement().use { statement ->
            statement.executeQuery(sql).use { result ->
                generateSequence { if (result.next()) result.getString(1) else null }.toList()
            }
        }
    }

    private companion object {

        const val ADDED = "classpath:db/reset-added"
        const val CHANGED = "classpath:db/reset-changed"
        const val ORIGINAL = "classpath:db/reset-original"
    }
}
