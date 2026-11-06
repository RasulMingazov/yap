package app.yap.server.config

import app.yap.server.core.database.DatabaseConfig
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class AppConfigLoaderTest {

    @Test
    fun `GIVEN no settings WHEN loading THEN the local defaults are used`() {
        val config = load(systemEnv = emptyMap())

        assertEquals(
            expected = AppConfig(
                database = DatabaseConfig(
                    password = "postgres",
                    poolSize = 10,
                    resetOnMigrationChange = false,
                    url = "jdbc:postgresql://localhost:5432/yap",
                    user = "postgres",
                ),
                environment = AppEnvironment.DEVELOPMENT,
                port = 8080,
            ),
            actual = config,
        )
    }

    @Test
    fun `GIVEN a port in the env file and in the system WHEN loading THEN the system value wins`() {
        val config = load(
            systemEnv = mapOf("PORT" to "9000"),
            dotEnv = "PORT=7000\nDATABASE_USER=\"yap\"\n",
        )

        assertEquals(expected = 9000 to "yap", actual = config.port to config.database.user)
    }

    @Test
    fun `GIVEN staging and the local password WHEN loading THEN startup fails`() {
        assertFailsWith<IllegalArgumentException> {
            load(systemEnv = mapOf("APP_ENV" to "staging"))
        }
    }

    @Test
    fun `GIVEN a port that is not a number WHEN loading THEN startup fails`() {
        assertFailsWith<IllegalArgumentException> {
            load(systemEnv = mapOf("PORT" to "eighty"))
        }
    }

    private fun load(systemEnv: Map<String, String>, dotEnv: String? = null): AppConfig {
        val directory = Files.createTempDirectory("yap-config")
        val dotEnvPath = directory.resolve(".env")
        if (dotEnv != null) Files.writeString(dotEnvPath, dotEnv)
        return AppConfigLoader(dotEnvPath = dotEnvPath, systemEnv = systemEnv::get).load()
            .also { deleteDirectory(directory) }
    }

    private fun deleteDirectory(directory: Path) {
        Files.deleteIfExists(directory.resolve(".env"))
        Files.deleteIfExists(directory)
    }
}
