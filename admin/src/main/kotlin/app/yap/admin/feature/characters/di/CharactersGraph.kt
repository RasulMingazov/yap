package app.yap.admin.feature.characters.di

import app.yap.admin.feature.characters.data.repository.RemoteCharacterRepository
import app.yap.admin.feature.characters.domain.usecase.DefaultGetCharactersUseCase
import app.yap.admin.feature.characters.presentation.CharactersViewModel
import app.yap.admin.feature.server.data.repository.PreferencesServerSettingsRepository
import io.ktor.client.HttpClient

internal fun charactersViewModel(httpClient: HttpClient): CharactersViewModel = CharactersViewModel(
    getCharactersUseCase = DefaultGetCharactersUseCase(
        characterRepository = RemoteCharacterRepository(
            httpClient = httpClient,
            serverSettingsRepository = PreferencesServerSettingsRepository(),
        ),
    ),
)
