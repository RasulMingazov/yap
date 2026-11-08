package app.yap.admin.feature.characters.presentation

import app.yap.admin.core.common.LoadResult
import app.yap.admin.feature.characters.domain.Character
import app.yap.admin.feature.characters.presentation.CharactersViewModel.DataState
import app.yap.admin.feature.characters.presentation.CharactersViewModel.UiState

internal object CharactersUiStateMapper : (DataState) -> UiState {

    override fun invoke(state: DataState): UiState = when (val characters = state.characters) {
        null -> UiState.Loading
        is LoadResult.Failed -> UiState.Error(message = "Не удалось загрузить персонажей")
        is LoadResult.Loaded -> characters.value.toUiState()
    }

    private fun List<Character>.toUiState(): UiState {
        if (isEmpty()) return UiState.Empty(message = "Персонажей пока нет")
        return UiState.Content(rows = map { character -> character.toRow() })
    }

    private fun Character.toRow(): UiState.Content.Row = UiState.Content.Row(
        characterId = characterId,
        name = profile.name,
        personality = profile.personality,
    )
}
