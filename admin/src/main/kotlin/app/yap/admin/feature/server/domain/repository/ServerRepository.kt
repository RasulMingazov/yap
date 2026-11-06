package app.yap.admin.feature.server.domain.repository

import app.yap.admin.feature.server.domain.ServerConnection
import app.yap.admin.feature.server.domain.ServerSettings

internal interface ServerRepository {

    suspend fun connect(settings: ServerSettings): ServerConnection
}
