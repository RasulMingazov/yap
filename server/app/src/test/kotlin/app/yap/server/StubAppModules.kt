package app.yap.server

import app.yap.server.core.llm.StubLlmConfig
import app.yap.server.core.llm.coreLlmModule
import app.yap.server.feature.characters.featureCharactersModule
import app.yap.server.feature.characters.persistence.CharacterRepository
import app.yap.server.feature.characters.persistence.StubCharacterRepository
import app.yap.server.feature.characters.usecase.CharacterTopicCounts
import app.yap.server.feature.characters.usecase.StubCharacterTopicCounts
import app.yap.server.feature.levels.featureLevelsModule
import app.yap.server.feature.levels.persistence.LevelRepository
import app.yap.server.feature.levels.persistence.StubLevelRepository
import app.yap.server.feature.models.featureModelsModule
import app.yap.server.feature.models.persistence.StubTaskModelRepository
import app.yap.server.feature.models.persistence.TaskModelRepository
import app.yap.server.feature.prompts.featurePromptsModule
import app.yap.server.feature.prompts.persistence.PromptRepository
import app.yap.server.feature.prompts.persistence.StubPromptRepository
import app.yap.server.feature.topics.featureTopicsModule
import app.yap.server.feature.topics.persistence.StubTopicRepository
import app.yap.server.feature.topics.persistence.TopicRepository
import org.koin.core.module.Module
import org.koin.dsl.module

internal object StubAppModules {

    fun stubAppModules(): List<Module> = listOf(
        coreLlmModule(config = StubLlmConfig.stubLlmConfig()),
        featureCharactersModule(),
        featureLevelsModule(),
        featureModelsModule(),
        featurePromptsModule(),
        featureTopicsModule(),
        module { single<CharacterRepository> { StubCharacterRepository() } },
        module { single<CharacterTopicCounts> { StubCharacterTopicCounts() } },
        module { single<LevelRepository> { StubLevelRepository() } },
        module { single<PromptRepository> { StubPromptRepository() } },
        module { single<TaskModelRepository> { StubTaskModelRepository() } },
        module { single<TopicRepository> { StubTopicRepository() } },
    )
}
