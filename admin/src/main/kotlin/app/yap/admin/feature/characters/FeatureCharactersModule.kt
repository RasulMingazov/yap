package app.yap.admin.feature.characters

import app.yap.admin.feature.characters.data.api.createCharacterApi
import app.yap.admin.feature.characters.data.repository.RemoteCharacterRepository
import app.yap.admin.feature.characters.domain.CharacterId
import app.yap.admin.feature.characters.domain.repository.CharacterRepository
import app.yap.admin.feature.characters.domain.usecase.DefaultGetCharacterUseCase
import app.yap.admin.feature.characters.domain.usecase.DefaultGetCharactersUseCase
import app.yap.admin.feature.characters.domain.usecase.DefaultSaveCharacterUseCase
import app.yap.admin.feature.characters.domain.usecase.GetCharacterUseCase
import app.yap.admin.feature.characters.domain.usecase.GetCharactersUseCase
import app.yap.admin.feature.characters.domain.usecase.SaveCharacterUseCase
import app.yap.admin.feature.characters.presentation.CharacterViewModel
import app.yap.admin.feature.characters.presentation.CharactersViewModel
import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal fun featureCharactersModule(): Module = module {
    single { get<Ktorfit>().createCharacterApi() }
    singleOf(::RemoteCharacterRepository) { bind<CharacterRepository>() }

    factoryOf(::DefaultGetCharacterUseCase) { bind<GetCharacterUseCase>() }
    factoryOf(::DefaultGetCharactersUseCase) { bind<GetCharactersUseCase>() }
    factoryOf(::DefaultSaveCharacterUseCase) { bind<SaveCharacterUseCase>() }

    factory { (characterId: CharacterId?) ->
        CharacterViewModel(
            characterId = characterId,
            getCharacterUseCase = get(),
            saveCharacterUseCase = get(),
        )
    }
    factoryOf(::CharactersViewModel)
}
