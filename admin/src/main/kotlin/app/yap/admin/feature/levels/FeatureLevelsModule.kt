package app.yap.admin.feature.levels

import app.yap.admin.feature.levels.data.api.createLevelApi
import app.yap.admin.feature.levels.data.repository.RemoteLevelRepository
import app.yap.admin.feature.levels.domain.repository.LevelRepository
import app.yap.admin.feature.levels.domain.usecase.DefaultGetLevelsUseCase
import app.yap.admin.feature.levels.domain.usecase.DefaultSaveLevelsUseCase
import app.yap.admin.feature.levels.domain.usecase.GetLevelsUseCase
import app.yap.admin.feature.levels.domain.usecase.SaveLevelsUseCase
import app.yap.admin.feature.levels.presentation.LevelsViewModel
import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal fun featureLevelsModule(): Module = module {
    single { get<Ktorfit>().createLevelApi() }
    singleOf(::RemoteLevelRepository) { bind<LevelRepository>() }

    factoryOf(::DefaultGetLevelsUseCase) { bind<GetLevelsUseCase>() }
    factoryOf(::DefaultSaveLevelsUseCase) { bind<SaveLevelsUseCase>() }

    factoryOf(::LevelsViewModel)
}
