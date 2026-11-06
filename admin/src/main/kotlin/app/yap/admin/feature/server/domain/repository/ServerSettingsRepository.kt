package app.yap.admin.feature.server.domain.repository

import app.yap.admin.feature.server.domain.ServerSettings

internal interface ServerSettingsRepository {

    suspend fun get(): ServerSettings?

    suspend fun save(settings: ServerSettings)
}
