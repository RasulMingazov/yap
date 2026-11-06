package app.yap.server.config

internal enum class AppEnvironment {
    DEVELOPMENT,
    TEST,
    STAGING,
    PRODUCTION;

    val isDeployed: Boolean
        get() = this == STAGING || this == PRODUCTION

    companion object {

        fun from(value: String): AppEnvironment = entries
            .firstOrNull { environment -> environment.name.equals(value, ignoreCase = true) }
            ?: throw IllegalArgumentException("APP_ENV must be one of: development, test, staging, production")
    }
}
