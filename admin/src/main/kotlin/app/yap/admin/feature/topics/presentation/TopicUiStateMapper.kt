package app.yap.admin.feature.topics.presentation

import app.yap.admin.core.presentation.Status
import app.yap.admin.feature.characters.domain.Character
import app.yap.admin.feature.topics.domain.Interlocutor
import app.yap.admin.feature.topics.presentation.TopicViewModel.DataState
import app.yap.admin.feature.topics.presentation.TopicViewModel.Load
import app.yap.admin.feature.topics.presentation.TopicViewModel.SaveStatus
import app.yap.admin.feature.topics.presentation.TopicViewModel.UiState
import app.yap.admin.feature.topics.presentation.TopicViewModel.UiState.Content.Choice
import java.time.LocalDate

internal object TopicUiStateMapper {

    fun map(state: DataState): UiState = when (state.load) {
        Load.FAILED -> UiState.Error(message = "Не удалось загрузить тему")
        Load.LOADED -> state.toContent()
        Load.LOADING -> UiState.Loading
    }

    private fun DataState.toContent(): UiState.Content = UiState.Content(
        choices = choices(),
        form = form,
        guestDescription = guestDescription(),
        hint = hint(),
        isSaveEnabled = !isSaving,
        releaseDateText = form.releaseDate.longText(),
        saveStatus = saveStatus(),
        subtitle = subtitle(),
        title = title(),
    )

    private fun DataState.choices(): List<Choice> {
        val castChoices = cast.map { character -> character.toChoice(form.interlocutor) }
        return castChoices + guestChoice(form.interlocutor)
    }

    private fun Character.toChoice(selected: Interlocutor?): Choice {
        val interlocutor = Interlocutor.Cast(characterId = characterId)
        return Choice(
            interlocutor = interlocutor,
            isSelected = interlocutor == selected,
            meta = null,
            name = profile.name,
        )
    }

    private fun guestChoice(selected: Interlocutor?): Choice {
        val guest = selected as? Interlocutor.Guest ?: Interlocutor.Guest(description = "")
        return Choice(
            interlocutor = guest,
            isSelected = selected is Interlocutor.Guest,
            meta = "Только для этой темы",
            name = "Гость",
        )
    }

    private fun DataState.guestDescription(): String? = (form.interlocutor as? Interlocutor.Guest)?.description

    private fun DataState.hint(): String? =
        if (saved == null) "Для черновика нужны название и дата выхода. Остальное можно заполнить позже." else null

    private fun LocalDate?.longText(): String = this?.let(ReleaseDateFormat::long).orEmpty()

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
