package app.yap.admin.feature.server.domain.usecase

import app.yap.admin.core.network.settings.ServerSettings
import app.yap.admin.core.network.settings.ServerSettingsRepository

internal interface GetServerSettingsUseCase {

    suspend operator fun invoke(): ServerSettings?
}

internal class DefaultGetServerSettingsUseCase(
    private val serverSettingsRepository: ServerSettingsRepository,
) : GetServerSettingsUseCase {

    override suspend fun invoke(): ServerSettings? = serverSettingsRepository.get()
}
