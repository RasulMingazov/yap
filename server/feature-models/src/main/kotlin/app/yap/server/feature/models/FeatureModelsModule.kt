package app.yap.server.feature.models

import app.yap.server.feature.models.persistence.DatabaseTaskModelRepository
import app.yap.server.feature.models.persistence.TaskModelRepository
import app.yap.server.feature.models.usecase.GetModelsUseCase
import app.yap.server.feature.models.usecase.GetTaskModelUseCase
import app.yap.server.feature.models.usecase.SaveModelsUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun featureModelsModule(): Module = module {
    singleOf(::DatabaseTaskModelRepository) { bind<TaskModelRepository>() }

    factoryOf(::GetModelsUseCase)
    factoryOf(::GetTaskModelUseCase)
    factoryOf(::SaveModelsUseCase)
}
