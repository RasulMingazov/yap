package app.yap.server.core.config

data class DatabaseConfig(
    val password: String,
    val poolSize: Int,
    val resetOnMigrationChange: Boolean,
    val url: String,
    val user: String,
) {

    init {
        require(url.startsWith("jdbc:postgresql://")) { "DATABASE_URL must be a PostgreSQL JDBC URL" }
        require(user.isNotBlank()) { "DATABASE_USER must not be blank" }
        require(poolSize in MIN_POOL_SIZE..MAX_POOL_SIZE) {
            "DATABASE_POOL_SIZE must be between $MIN_POOL_SIZE and $MAX_POOL_SIZE"
        }
    }

    fun validateFor(environment: AppEnvironment) {
        if (environment.isDeployed) {
            require(password.isNotBlank() && password != LOCAL_PASSWORD) {
                "DATABASE_PASSWORD must be configured for staging and production"
            }
        }
    }

    companion object {

        const val LOCAL_PASSWORD = "postgres"
        private const val MAX_POOL_SIZE = 100
        private const val MIN_POOL_SIZE = 1
    }
}
