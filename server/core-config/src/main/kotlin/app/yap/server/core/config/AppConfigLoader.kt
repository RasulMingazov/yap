package app.yap.server.core.config

import java.nio.file.Files
import java.nio.file.Path

class AppConfigLoader(
    private val dotEnvPath: Path = Path.of(".env"),
    private val systemEnv: (String) -> String? = System::getenv,
) {

    private val dotEnv: Map<String, String> by lazy(::readDotEnv)

    fun load(): AppConfig {
        val environment = AppEnvironment.from(env("APP_ENV") ?: "development")
        val database = DatabaseConfig(
            password = env("DATABASE_PASSWORD") ?: DatabaseConfig.LOCAL_PASSWORD,
            poolSize = intEnv(name = "DATABASE_POOL_SIZE", default = 10),
            resetOnMigrationChange = booleanEnv(name = "DATABASE_RESET_ON_MIGRATION_CHANGE", default = false),
            url = env("DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/yap",
            user = env("DATABASE_USER") ?: "postgres",
        ).also { config -> config.validateFor(environment) }
        return AppConfig(
            database = database,
            environment = environment,
            port = intEnv(name = "PORT", default = 8080),
        )
    }

    private fun env(name: String): String? = systemEnv(name) ?: dotEnv[name]

    private fun intEnv(name: String, default: Int): Int = env(name)
        ?.let { value -> value.toIntOrNull() ?: throw IllegalArgumentException("$name must be an integer") }
        ?: default

    private fun booleanEnv(name: String, default: Boolean): Boolean = env(name)
        ?.let { value ->
            value.trim()
                .lowercase()
                .toBooleanStrictOrNull()
                ?: throw IllegalArgumentException("$name must be true or false")
        }
        ?: default

    private fun readDotEnv(): Map<String, String> {
        if (!Files.exists(dotEnvPath)) return emptyMap()
        return Files.readAllLines(dotEnvPath)
            .asSequence()
            .map(String::trim)
            .filter { line -> line.isNotEmpty() && !line.startsWith('#') && '=' in line }
            .associate { line ->
                val separator = line.indexOf('=')
                line.substring(0, separator).trim() to line.substring(separator + 1).trim().removeSurrounding("\"")
            }
    }
}
