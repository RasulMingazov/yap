package app.yap.admin.feature.server

import app.yap.admin.feature.server.data.repository.RemoteServerRepository
import app.yap.admin.feature.server.domain.repository.ServerRepository
import app.yap.admin.feature.server.domain.usecase.ConnectToServerUseCase
import app.yap.admin.feature.server.domain.usecase.DefaultConnectToServerUseCase
import app.yap.admin.feature.server.domain.usecase.DefaultGetServerSettingsUseCase
import app.yap.admin.feature.server.domain.usecase.GetServerSettingsUseCase
import app.yap.admin.feature.server.presentation.ServerViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal fun featureServerModule(): Module = module {
    singleOf(::RemoteServerRepository) { bind<ServerRepository>() }

    factoryOf(::DefaultConnectToServerUseCase) { bind<ConnectToServerUseCase>() }
    factoryOf(::DefaultGetServerSettingsUseCase) { bind<GetServerSettingsUseCase>() }

    factoryOf(::ServerViewModel)
}
