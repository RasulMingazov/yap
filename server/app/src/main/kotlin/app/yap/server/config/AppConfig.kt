package app.yap.server.config

import app.yap.server.core.database.DatabaseConfig

internal data class AppConfig(
    val database: DatabaseConfig,
    val environment: AppEnvironment,
    val port: Int,
) {

    init {
        require(port in MIN_PORT..MAX_PORT) { "PORT must be between $MIN_PORT and $MAX_PORT" }
        if (environment.isDeployed) {
            require(database.password.isNotBlank() && database.password != DatabaseConfig.LOCAL_PASSWORD) {
                "DATABASE_PASSWORD must be configured for staging and production"
            }
        }
    }

    private companion object {

        const val MAX_PORT = 65535
        const val MIN_PORT = 1
    }
}
