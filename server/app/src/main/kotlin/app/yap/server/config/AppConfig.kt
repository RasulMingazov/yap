package app.yap.server.config

import app.yap.server.core.database.DatabaseConfig
import app.yap.server.core.llm.LlmConfig

internal data class AppConfig(
    val adminApiKey: String,
    val database: DatabaseConfig,
    val environment: AppEnvironment,
    val llm: LlmConfig,
    val port: Int,
) {

    init {
        require(port in MIN_PORT..MAX_PORT) { "PORT must be between $MIN_PORT and $MAX_PORT" }
        require(adminApiKey.isNotBlank()) { "ADMIN_API_KEY must not be blank" }
        if (environment.isDeployed) {
            require(adminApiKey != LOCAL_ADMIN_API_KEY) { "ADMIN_API_KEY must be configured for staging and production" }
            require(database.password.isNotBlank() && database.password != LOCAL_DATABASE_PASSWORD) {
                "DATABASE_PASSWORD must be configured for staging and production"
            }
        }
    }

    companion object {

        const val LOCAL_ADMIN_API_KEY = "local-admin-key"
        const val LOCAL_DATABASE_PASSWORD = "postgres"
        private const val MAX_PORT = 65535
        private const val MIN_PORT = 1
    }
}
