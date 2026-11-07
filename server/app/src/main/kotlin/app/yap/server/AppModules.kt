package app.yap.server

import app.yap.server.config.AppConfig
import app.yap.server.core.database.coreDatabaseModule
import app.yap.server.feature.characters.featureCharactersModule
import app.yap.server.feature.levels.featureLevelsModule
import app.yap.server.feature.topics.featureTopicsModule
import org.koin.core.module.Module

internal fun appModules(config: AppConfig): List<Module> = listOf(
    coreDatabaseModule(config.database),
    featureCharactersModule(),
    featureLevelsModule(),
    featureTopicsModule(),
)
