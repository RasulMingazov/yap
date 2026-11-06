package app.yap.server.core.config

data class AppConfig(
    val database: DatabaseConfig,
    val environment: AppEnvironment,
    val port: Int,
) {

    init {
        require(port in MIN_PORT..MAX_PORT) { "PORT must be between $MIN_PORT and $MAX_PORT" }
    }

    private companion object {

        const val MAX_PORT = 65535
        const val MIN_PORT = 1
    }
}
