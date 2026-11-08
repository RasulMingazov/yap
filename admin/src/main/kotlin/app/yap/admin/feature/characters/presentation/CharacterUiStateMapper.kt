package app.yap.admin.feature.characters.presentation

import app.yap.admin.core.presentation.Status
import app.yap.admin.feature.characters.domain.SaveCharacterResult
import app.yap.admin.feature.characters.presentation.CharacterViewModel.DataState
import app.yap.admin.feature.characters.presentation.CharacterViewModel.UiState

internal object CharacterUiStateMapper : (DataState) -> UiState {

    override fun invoke(state: DataState): UiState = when {
        state.isLoadFailed -> UiState.Error(message = "Не удалось загрузить персонажа")
        state.characterId != null && state.savedProfile == null -> UiState.Loading
        else -> state.toContent()
    }

    private fun DataState.toContent(): UiState.Content = UiState.Content(
        form = form,
        hint = hint(),
        isSaveEnabled = !isSaving,
        saveStatus = saveResult?.status(),
        subtitle = subtitle(),
        title = savedProfile?.name ?: "Новый персонаж",
    )

    private fun DataState.hint(): String? = if (savedProfile == null) "Заполните поля и нажмите «Сохранить»." else null

    private fun SaveCharacterResult.status(): Status? = when (this) {
        is SaveCharacterResult.Failed -> null
        is SaveCharacterResult.Invalid -> Status(text = "Заполните все поля", tone = Status.Tone.ERROR)
        is SaveCharacterResult.Saved -> Status(text = "Сохранено", tone = Status.Tone.SUCCESS)
    }

    private fun DataState.subtitle(): String = if (savedProfile == null) "Ещё не сохранён" else "Постоянный герой"
}
