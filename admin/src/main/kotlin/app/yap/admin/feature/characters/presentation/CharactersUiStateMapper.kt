package app.yap.admin.feature.characters.presentation

import app.yap.admin.feature.characters.domain.CharacterSummary
import app.yap.admin.feature.characters.presentation.CharactersViewModel.DataState
import app.yap.admin.feature.characters.presentation.CharactersViewModel.UiState

internal object CharactersUiStateMapper : (DataState) -> UiState {

    override fun invoke(state: DataState): UiState {
        val characters = state.characters
        return when {
            characters != null -> characters.toUiState()
            state.isLoadFailed -> UiState.Error(message = "Не удалось загрузить персонажей")
            else -> UiState.Loading
        }
    }

    private fun List<CharacterSummary>.toUiState(): UiState {
        if (isEmpty()) return UiState.Empty(message = "Персонажей пока нет")
        return UiState.Content(rows = map { character -> character.toRow() })
    }

    private fun CharacterSummary.toRow(): UiState.Content.Row = UiState.Content.Row(
        characterId = characterId,
        name = profile.name,
        personality = profile.personality,
        topicCountText = TopicCountText(topicCount),
    )
}
