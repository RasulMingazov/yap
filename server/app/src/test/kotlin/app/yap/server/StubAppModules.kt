package app.yap.server

import app.yap.server.feature.characters.featureCharactersModule
import app.yap.server.feature.characters.persistence.CharacterRepository
import app.yap.server.feature.characters.persistence.StubCharacterRepository
import app.yap.server.feature.levels.featureLevelsModule
import app.yap.server.feature.levels.persistence.LevelRepository
import app.yap.server.feature.levels.persistence.StubLevelRepository
import app.yap.server.feature.topics.featureTopicsModule
import app.yap.server.feature.topics.persistence.StubTopicRepository
import app.yap.server.feature.topics.persistence.TopicRepository
import org.koin.core.module.Module
import org.koin.dsl.module

internal object StubAppModules {

    fun stubAppModules(): List<Module> = listOf(
        featureCharactersModule(),
        featureLevelsModule(),
        featureTopicsModule(),
        module { single<CharacterRepository> { StubCharacterRepository() } },
        module { single<LevelRepository> { StubLevelRepository() } },
        module { single<TopicRepository> { StubTopicRepository() } },
    )
}
