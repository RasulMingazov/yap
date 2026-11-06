package app.yap.admin.feature.characters.di

import app.yap.admin.feature.characters.data.repository.DefaultCharacterRepository
import app.yap.admin.feature.characters.domain.CharacterId
import app.yap.admin.feature.characters.domain.usecase.DefaultGetCharacterUseCase
import app.yap.admin.feature.characters.domain.usecase.DefaultGetCharactersUseCase
import app.yap.admin.feature.characters.domain.usecase.DefaultSaveCharacterUseCase
import app.yap.admin.feature.characters.presentation.CharacterViewModel
import app.yap.admin.feature.characters.presentation.CharactersViewModel
import app.yap.admin.feature.server.data.repository.DefaultServerSettingsRepository
import io.ktor.client.HttpClient

internal fun characterViewModel(
    characterId: CharacterId?,
    httpClient: HttpClient,
): CharacterViewModel {
    val characterRepository = characterRepository(httpClient)
    return CharacterViewModel(
        characterId = characterId,
        getCharacterUseCase = DefaultGetCharacterUseCase(characterRepository = characterRepository),
        saveCharacterUseCase = DefaultSaveCharacterUseCase(characterRepository = characterRepository),
    )
}

internal fun charactersViewModel(httpClient: HttpClient): CharactersViewModel = CharactersViewModel(
    getCharactersUseCase = DefaultGetCharactersUseCase(characterRepository = characterRepository(httpClient)),
)

private fun characterRepository(httpClient: HttpClient): DefaultCharacterRepository = DefaultCharacterRepository(
    httpClient = httpClient,
    serverSettingsRepository = DefaultServerSettingsRepository(),
)
