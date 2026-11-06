package app.yap.server.core.database

import org.junit.jupiter.api.Assumptions.assumeTrue
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

fun <T> withTestDatabase(block: (Database) -> T): T = withPostgres { container ->
    openDatabase(
        DatabaseConfig(
            password = container.password,
            poolSize = 2,
            resetOnMigrationChange = false,
            url = container.jdbcUrl,
            user = container.username,
        ),
    )
        .use(block)
}

fun <T> withPostgres(block: (PostgreSQLContainer<*>) -> T): T {
    assumeDocker()
    return PostgreSQLContainer(DockerImageName.parse(POSTGRES_IMAGE)).use { container ->
        container.start()
        block(container)
    }
}

private fun assumeDocker() {
    val available = runCatching { DockerClientFactory.instance().isDockerAvailable }.getOrDefault(false)
    check(available || System.getenv("CI") != "true") { "Docker is not available in CI: the PostgreSQL suite must run" }
    assumeTrue(available, "Docker is not available: the PostgreSQL suite did not run")
}

private const val POSTGRES_IMAGE = "postgres:18-alpine"
