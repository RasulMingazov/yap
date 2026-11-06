package app.yap.server.core.database

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

    companion object {

        private const val MAX_POOL_SIZE = 100
        private const val MIN_POOL_SIZE = 1
    }
}
