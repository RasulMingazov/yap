package app.yap.admin.feature.server.domain.usecase

import app.yap.admin.core.network.settings.ServerSettings
import app.yap.admin.core.network.settings.ServerSettingsRepository
import app.yap.admin.feature.server.domain.ServerConnection
import app.yap.admin.feature.server.domain.repository.ServerRepository

internal interface ConnectToServerUseCase {

    suspend operator fun invoke(settings: ServerSettings): ServerConnection
}

internal class DefaultConnectToServerUseCase(
    private val serverRepository: ServerRepository,
    private val serverSettingsRepository: ServerSettingsRepository,
) : ConnectToServerUseCase {

    override suspend fun invoke(settings: ServerSettings): ServerConnection {
        val trimmed = ServerSettings(
            address = settings.address.trim(),
            key = settings.key.trim(),
        )
        val connection = serverRepository.connect(trimmed)
        if (connection is ServerConnection.Connected) serverSettingsRepository.save(trimmed)
        return connection
    }
}
