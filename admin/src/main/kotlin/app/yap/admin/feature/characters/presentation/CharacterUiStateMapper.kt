package app.yap.admin.feature.characters.presentation

import app.yap.admin.core.presentation.Status
import app.yap.admin.feature.characters.presentation.CharacterViewModel.DataState
import app.yap.admin.feature.characters.presentation.CharacterViewModel.Load
import app.yap.admin.feature.characters.presentation.CharacterViewModel.SaveStatus
import app.yap.admin.feature.characters.presentation.CharacterViewModel.UiState

internal object CharacterUiStateMapper {

    fun map(state: DataState): UiState = when (state.load) {
        Load.FAILED -> UiState.Error(message = "Не удалось загрузить персонажа")
        Load.LOADED -> state.toContent()
        Load.LOADING -> UiState.Loading
    }

    private fun DataState.toContent(): UiState.Content = UiState.Content(
        form = form,
        hint = hint(),
        isSaveEnabled = !isSaving,
        saveStatus = saveStatus?.status(),
        subtitle = subtitle(),
        title = savedName ?: "Новый персонаж",
    )

    private fun DataState.hint(): String? = if (savedName == null) "Заполните поля и нажмите «Сохранить»." else null

    private fun SaveStatus.status(): Status = when (this) {
        SaveStatus.EMPTY_FIELD -> Status(text = "Заполните все поля", tone = Status.Tone.ERROR)
        SaveStatus.SAVED -> Status(text = "Сохранено", tone = Status.Tone.SUCCESS)
    }

    private fun DataState.subtitle(): String = if (savedName == null) "Ещё не сохранён" else "Постоянный герой"
}
