package app.yap.server.core.database

import kotlin.test.Test
import kotlin.test.assertFailsWith
import org.koin.core.error.InstanceCreationException
import org.koin.dsl.koinApplication

internal class CoreDatabaseModuleTest {

    @Test
    fun `GIVEN an unreachable database WHEN Koin starts THEN the start fails`() {
        val config = DatabaseConfig(
            password = "test-password",
            poolSize = 1,
            resetOnMigrationChange = false,
            url = "jdbc:postgresql://localhost:1/yap",
            user = "postgres",
        )

        assertFailsWith<InstanceCreationException> {
            koinApplication { modules(coreDatabaseModule(config)) }
        }
    }
}
