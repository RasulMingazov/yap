package app.yap.server.feature.prompts

import app.yap.server.feature.prompts.persistence.DatabasePromptRepository
import app.yap.server.feature.prompts.persistence.PromptRepository
import app.yap.server.feature.prompts.usecase.GetPromptUseCase
import app.yap.server.feature.prompts.usecase.GetPromptsUseCase
import app.yap.server.feature.prompts.usecase.RestorePromptUseCase
import app.yap.server.feature.prompts.usecase.SavePromptUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun featurePromptsModule(): Module = module {
    singleOf(::DatabasePromptRepository) { bind<PromptRepository>() }

    factoryOf(::GetPromptUseCase)
    factoryOf(::GetPromptsUseCase)
    factoryOf(::RestorePromptUseCase)
    factoryOf(::SavePromptUseCase)
}
