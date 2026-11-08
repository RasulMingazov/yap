package app.yap.admin.core.network.settings

internal interface ServerSettingsRepository {

    suspend fun get(): ServerSettings?

    suspend fun save(settings: ServerSettings)
}
