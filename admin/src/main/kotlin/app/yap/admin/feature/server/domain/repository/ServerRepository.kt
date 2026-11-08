package app.yap.admin.feature.server.domain.repository

import app.yap.admin.core.network.settings.ServerSettings
import app.yap.admin.feature.server.domain.ServerConnection

internal interface ServerRepository {

    suspend fun connect(settings: ServerSettings): ServerConnection
}
