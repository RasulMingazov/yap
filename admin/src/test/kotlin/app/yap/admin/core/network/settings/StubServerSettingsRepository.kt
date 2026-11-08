package app.yap.admin.core.network.settings

import io.github.rasulmingazov.stubcall.StubCall0
import io.github.rasulmingazov.stubcall.StubCall1

internal class StubServerSettingsRepository(
    settings: ServerSettings? = null,
) : ServerSettingsRepository {

    val getCall = StubCall0.returns<ServerSettings?>(settings)
    val saveCall = StubCall1.unit<ServerSettings>()

    override suspend fun get(): ServerSettings? = getCall.invoke()

    override suspend fun save(settings: ServerSettings) = saveCall.invoke(settings)
}
