package app.yap.admin.feature.server.domain.usecase

import app.yap.admin.core.network.settings.ServerSettings
import app.yap.admin.feature.server.domain.ServerConnection
import io.github.rasulmingazov.stubcall.StubCall1

internal class StubConnectToServerUseCase(
    connection: ServerConnection = ServerConnection.Connected(environment = "development"),
) : ConnectToServerUseCase {

    val invokeCall = StubCall1.returns<ServerSettings, ServerConnection>(connection)

    override suspend fun invoke(settings: ServerSettings): ServerConnection = invokeCall.invoke(settings)
}
