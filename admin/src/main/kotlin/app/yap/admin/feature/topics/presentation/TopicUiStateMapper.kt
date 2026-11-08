package app.yap.admin.feature.topics.presentation

import app.yap.admin.core.presentation.Status
import app.yap.admin.feature.topics.presentation.TopicViewModel.DataState
import app.yap.admin.feature.topics.presentation.TopicViewModel.Load
import app.yap.admin.feature.topics.presentation.TopicViewModel.SaveStatus
import app.yap.admin.feature.topics.presentation.TopicViewModel.UiState

internal object TopicUiStateMapper {

    fun map(state: DataState): UiState = when (state.load) {
        Load.FAILED -> UiState.Error(message = "Не удалось загрузить тему")
        Load.LOADED -> state.toContent()
        Load.LOADING -> UiState.Loading
    }

    private fun DataState.toContent(): UiState.Content = UiState.Content(
        castNote = InterlocutorUiStateMapper.castNote(this),
        choices = InterlocutorUiStateMapper.choices(this),
        form = form,
        guestDescription = InterlocutorUiStateMapper.guestDescription(this),
        hint = hint(),
        isSaveEnabled = !isSaving,
        releaseDateText = form.releaseDate?.let(ReleaseDateFormat::long),
        saveStatus = saveStatus(),
        subtitle = subtitle(),
        title = title(),
    )

    private fun DataState.hint(): String? =
        if (saved == null) "Для черновика нужны название и дата выхода. Остальное можно заполнить позже." else null

    private fun DataState.saveStatus(): Status? =
        if (isSaving) Status(text = "Сохранение…", tone = Status.Tone.MUTED) else saveStatus?.status()

    private fun SaveStatus.status(): Status = when (this) {
        SaveStatus.REJECTED -> Status(
            text = "Сервер не принял черновик: проверьте название, дату, длительность и собеседника",
            tone = Status.Tone.ERROR,
        )
        SaveStatus.SAVED -> Status(text = "Сохранено", tone = Status.Tone.SUCCESS)
    }

    private fun DataState.subtitle(): String {
        val content = saved?.content ?: return "Ещё не сохранена"
        val releaseDate = content.releaseDate?.let(ReleaseDateFormat::long)
        return listOfNotNull(releaseDate, "черновик").joinToString(separator = " · ")
    }

    private fun DataState.title(): String = saved?.content?.title ?: "Новая тема"
}
