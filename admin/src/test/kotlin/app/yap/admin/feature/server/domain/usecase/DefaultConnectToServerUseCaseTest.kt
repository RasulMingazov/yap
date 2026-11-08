package app.yap.admin.feature.server.domain.usecase

import app.yap.admin.core.network.settings.StubServerSettings
import app.yap.admin.core.network.settings.StubServerSettingsRepository
import app.yap.admin.feature.server.domain.ServerConnection
import app.yap.admin.feature.server.domain.repository.StubServerRepository
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

internal class DefaultConnectToServerUseCaseTest {

    @Test
    fun `GIVEN the server accepts the key WHEN connecting THEN the settings are saved`() = runTest {
        val settings = StubServerSettings.stubServerSettings()
        val env = Environment(connection = ServerConnection.Connected(environment = "staging"))

        env.useCase(settings)

        env.serverSettingsRepository.saveCall.calledWith(settings)
    }

    @Test
    fun `GIVEN the server rejects the key WHEN connecting THEN the settings are not saved`() = runTest {
        val env = Environment(connection = ServerConnection.Unauthorized)

        env.useCase(StubServerSettings.stubServerSettings())

        env.serverSettingsRepository.saveCall.notCalled()
    }

    @Test
    fun `GIVEN settings with spaces around WHEN connecting THEN it connects with the trimmed settings`() = runTest {
        val env = Environment(connection = ServerConnection.Unreachable)

        env.useCase(StubServerSettings.stubServerSettings(address = " http://staging.yap ", key = " staging-key "))

        env.serverRepository.connectCall.calledWith(
            StubServerSettings.stubServerSettings(address = "http://staging.yap", key = "staging-key"),
        )
    }

    @Test
    fun `GIVEN settings with spaces around WHEN the server accepts the key THEN the trimmed settings are saved`() =
        runTest {
            val env = Environment(connection = ServerConnection.Connected(environment = "staging"))

            env.useCase(StubServerSettings.stubServerSettings(address = " http://staging.yap ", key = " staging-key "))

            env.serverSettingsRepository.saveCall.calledWith(
                StubServerSettings.stubServerSettings(address = "http://staging.yap", key = "staging-key"),
            )
        }

    private class Environment(
        connection: ServerConnection,
    ) {

        val serverRepository = StubServerRepository(connection = connection)
        val serverSettingsRepository = StubServerSettingsRepository()
        val useCase = DefaultConnectToServerUseCase(
            serverRepository = serverRepository,
            serverSettingsRepository = serverSettingsRepository,
        )
    }
}
