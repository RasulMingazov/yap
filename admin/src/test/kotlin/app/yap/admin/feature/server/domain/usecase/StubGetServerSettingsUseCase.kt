package app.yap.admin.feature.server.domain.usecase

import app.yap.admin.core.network.settings.ServerSettings
import io.github.rasulmingazov.stubcall.StubCall0

internal class StubGetServerSettingsUseCase(
    settings: ServerSettings? = null,
) : GetServerSettingsUseCase {

    val invokeCall = StubCall0.returns<ServerSettings?>(settings)

    override suspend fun invoke(): ServerSettings? = invokeCall.invoke()
}
