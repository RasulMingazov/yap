package app.yap.server

import app.yap.server.feature.characters.featureCharactersModule
import app.yap.server.feature.characters.persistence.CharacterRepository
import app.yap.server.feature.characters.persistence.StubCharacterRepository
import org.koin.core.module.Module
import org.koin.dsl.module

internal object StubAppModules {

    fun stubAppModules(
        characterRepository: CharacterRepository = StubCharacterRepository(),
    ): List<Module> = listOf(
        featureCharactersModule(),
        module { single<CharacterRepository> { characterRepository } },
    )
}
