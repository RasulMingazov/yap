package app.yap.server

import app.yap.server.config.AppConfig
import app.yap.server.config.AppEnvironment
import app.yap.server.core.database.DatabaseConfig

internal object StubAppConfig {

    const val ADMIN_API_KEY = "test-admin-key"

    fun stubAppConfig(
        adminApiKey: String = ADMIN_API_KEY,
        environment: AppEnvironment = AppEnvironment.TEST,
    ): AppConfig = AppConfig(
        adminApiKey = adminApiKey,
        database = DatabaseConfig(
            password = "test-password",
            poolSize = 1,
            resetOnMigrationChange = false,
            url = "jdbc:postgresql://localhost:5432/yap",
            user = "postgres",
        ),
        environment = environment,
        port = 8080,
    )
}
