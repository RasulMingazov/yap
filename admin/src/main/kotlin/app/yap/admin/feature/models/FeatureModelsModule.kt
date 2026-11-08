package app.yap.admin.feature.models

import app.yap.admin.feature.models.data.api.createModelApi
import app.yap.admin.feature.models.data.repository.RemoteModelRepository
import app.yap.admin.feature.models.domain.repository.ModelRepository
import app.yap.admin.feature.models.domain.usecase.DefaultGetModelsUseCase
import app.yap.admin.feature.models.domain.usecase.DefaultSaveModelsUseCase
import app.yap.admin.feature.models.domain.usecase.GetModelsUseCase
import app.yap.admin.feature.models.domain.usecase.SaveModelsUseCase
import app.yap.admin.feature.models.presentation.ModelsViewModel
import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal fun featureModelsModule(): Module = module {
    single { get<Ktorfit>().createModelApi() }
    singleOf(::RemoteModelRepository) { bind<ModelRepository>() }

    factoryOf(::DefaultGetModelsUseCase) { bind<GetModelsUseCase>() }
    factoryOf(::DefaultSaveModelsUseCase) { bind<SaveModelsUseCase>() }

    factoryOf(::ModelsViewModel)
}
