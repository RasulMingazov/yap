package app.yap.server.feature.levels

import app.yap.server.feature.levels.persistence.DatabaseLevelRepository
import app.yap.server.feature.levels.persistence.LevelRepository
import app.yap.server.feature.levels.usecase.GetLevelRulesUseCase
import app.yap.server.feature.levels.usecase.GetLevelsUseCase
import app.yap.server.feature.levels.usecase.SaveLevelsUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun featureLevelsModule(): Module = module {
    singleOf(::DatabaseLevelRepository) { bind<LevelRepository>() }

    factoryOf(::GetLevelRulesUseCase)
    factoryOf(::GetLevelsUseCase)
    factoryOf(::SaveLevelsUseCase)
}
