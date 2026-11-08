package app.yap.admin.feature.server.domain.repository

import app.yap.admin.core.network.settings.ServerSettings
import app.yap.admin.feature.server.domain.ServerConnection
import io.github.rasulmingazov.stubcall.StubCall1

internal class StubServerRepository(
    connection: ServerConnection = ServerConnection.Connected(environment = "development"),
) : ServerRepository {

    val connectCall = StubCall1.returns<ServerSettings, ServerConnection>(connection)

    override suspend fun connect(settings: ServerSettings): ServerConnection = connectCall.invoke(settings)
}
