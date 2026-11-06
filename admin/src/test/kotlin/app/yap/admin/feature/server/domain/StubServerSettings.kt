package app.yap.admin.feature.server.domain

internal object StubServerSettings {

    const val ADDRESS = "http://localhost:8080"
    const val KEY = "local-admin-key"

    fun stubServerSettings(
        address: String = ADDRESS,
        key: String = KEY,
    ): ServerSettings = ServerSettings(
        address = address,
        key = key,
    )
}
