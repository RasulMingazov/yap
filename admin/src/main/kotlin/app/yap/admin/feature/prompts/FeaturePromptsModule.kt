package app.yap.admin.feature.prompts

import app.yap.admin.feature.prompts.data.api.createPromptApi
import app.yap.admin.feature.prompts.data.repository.RemotePromptRepository
import app.yap.admin.feature.prompts.domain.repository.PromptRepository
import app.yap.admin.feature.prompts.domain.usecase.DefaultGetPromptUseCase
import app.yap.admin.feature.prompts.domain.usecase.DefaultGetPromptsUseCase
import app.yap.admin.feature.prompts.domain.usecase.DefaultRestorePromptUseCase
import app.yap.admin.feature.prompts.domain.usecase.DefaultSavePromptUseCase
import app.yap.admin.feature.prompts.domain.usecase.GetPromptUseCase
import app.yap.admin.feature.prompts.domain.usecase.GetPromptsUseCase
import app.yap.admin.feature.prompts.domain.usecase.RestorePromptUseCase
import app.yap.admin.feature.prompts.domain.usecase.SavePromptUseCase
import app.yap.admin.feature.prompts.presentation.PromptsViewModel
import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal fun featurePromptsModule(): Module = module {
    single { get<Ktorfit>().createPromptApi() }
    singleOf(::RemotePromptRepository) { bind<PromptRepository>() }

    factoryOf(::DefaultGetPromptUseCase) { bind<GetPromptUseCase>() }
    factoryOf(::DefaultGetPromptsUseCase) { bind<GetPromptsUseCase>() }
    factoryOf(::DefaultRestorePromptUseCase) { bind<RestorePromptUseCase>() }
    factoryOf(::DefaultSavePromptUseCase) { bind<SavePromptUseCase>() }

    factoryOf(::PromptsViewModel)
}
