package app.yap.admin

import app.yap.admin.core.network.coreNetworkModule
import app.yap.admin.feature.characters.featureCharactersModule
import app.yap.admin.feature.server.featureServerModule
import app.yap.admin.navigation.navigationModule
import org.koin.core.module.Module

internal fun appModules(): List<Module> = listOf(
    coreNetworkModule(),
    featureCharactersModule(),
    featureServerModule(),
    navigationModule(),
)
