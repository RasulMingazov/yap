package app.yap.server.core.config

data class AppConfig(
    val adminApiKey: String,
    val database: DatabaseConfig,
    val environment: AppEnvironment,
    val port: Int,
) {

    init {
        require(port in MIN_PORT..MAX_PORT) { "PORT must be between $MIN_PORT and $MAX_PORT" }
        require(adminApiKey.isNotBlank()) { "ADMIN_API_KEY must not be blank" }
        if (environment.isDeployed) {
            require(adminApiKey != LOCAL_ADMIN_API_KEY) { "ADMIN_API_KEY must be configured for staging and production" }
        }
    }

    companion object {

        const val LOCAL_ADMIN_API_KEY = "local-admin-key"
        private const val MAX_PORT = 65535
        private const val MIN_PORT = 1
    }
}
