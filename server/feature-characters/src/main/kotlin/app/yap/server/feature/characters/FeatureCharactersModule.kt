package app.yap.server.feature.characters

import app.yap.server.feature.characters.persistence.CharacterRepository
import app.yap.server.feature.characters.persistence.DatabaseCharacterRepository
import app.yap.server.feature.characters.usecase.CreateCharacterUseCase
import app.yap.server.feature.characters.usecase.GetCharacterSummariesUseCase
import app.yap.server.feature.characters.usecase.GetCharacterUseCase
import app.yap.server.feature.characters.usecase.GetCharactersUseCase
import app.yap.server.feature.characters.usecase.UpdateCharacterUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun featureCharactersModule(): Module = module {
    singleOf(::DatabaseCharacterRepository) { bind<CharacterRepository>() }

    factoryOf(::CreateCharacterUseCase)
    factoryOf(::GetCharacterSummariesUseCase)
    factoryOf(::GetCharacterUseCase)
    factoryOf(::GetCharactersUseCase)
    factoryOf(::UpdateCharacterUseCase)
}
