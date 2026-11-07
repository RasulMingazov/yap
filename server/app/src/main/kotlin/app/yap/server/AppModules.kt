package app.yap.server

import app.yap.server.config.AppConfig
import app.yap.server.core.database.coreDatabaseModule
import app.yap.server.feature.characters.featureCharactersModule
import org.koin.core.module.Module

internal fun appModules(config: AppConfig): List<Module> = listOf(
    coreDatabaseModule(config.database),
    featureCharactersModule(),
)
