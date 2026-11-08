package app.yap.admin.feature.topics.presentation

import app.yap.admin.core.presentation.Status
import app.yap.admin.feature.characters.domain.CharacterSummary
import app.yap.admin.feature.characters.presentation.TopicCountText
import app.yap.admin.feature.topics.domain.Interlocutor
import app.yap.admin.feature.topics.presentation.TopicViewModel.DataState
import app.yap.admin.feature.topics.presentation.TopicViewModel.Load
import app.yap.admin.feature.topics.presentation.TopicViewModel.SaveStatus
import app.yap.admin.feature.topics.presentation.TopicViewModel.UiState
import app.yap.admin.feature.topics.presentation.TopicViewModel.UiState.Content.Choice

internal object TopicUiStateMapper : (DataState) -> UiState {

    override fun invoke(state: DataState): UiState = when (state.load) {
        Load.FAILED -> UiState.Error(
            message = "Не удалось загрузить тему",
            toolbar = state.placeholderToolbar(),
        )
        Load.LOADED -> state.toContent()
        Load.LOADING -> UiState.Loading(toolbar = state.placeholderToolbar())
    }

    private fun DataState.placeholderToolbar(): UiState.Toolbar = if (topicId == null) {
        UiState.Toolbar(
            subtitle = form.releaseDate?.let(ReleaseDateFormat::long),
            title = "Новая тема",
        )
    } else {
        UiState.Toolbar(
            subtitle = null,
            title = "Тема",
        )
    }

    private fun DataState.toContent(): UiState.Content = UiState.Content(
        castNote = castNote(),
        choices = choices(),
        form = form,
        guestDescription = formGuestDescription(),
        hint = hint(),
        isSaveEnabled = !isSaving,
        releaseDateText = form.releaseDate?.let(ReleaseDateFormat::long),
        saveStatus = saveStatus(),
        subtitle = subtitle(),
        title = title(),
    )

    private fun DataState.castNote(): String? =
        if (cast.isEmpty()) "Персонажей пока нет. Добавьте их в разделе «Персонажи»" else null

    private fun DataState.choices(): List<Choice> {
        val castChoices = cast.map { character -> character.toChoice(form.interlocutor) }
        return castChoices + guestChoice()
    }

    private fun CharacterSummary.toChoice(selected: Interlocutor?): Choice {
        val interlocutor = Interlocutor.Cast(characterId = characterId)
        return Choice(
            interlocutor = interlocutor,
            isSelected = interlocutor == selected,
            meta = TopicCountText(topicCount),
            name = profile.name,
        )
    }

    private fun DataState.guestChoice(): Choice = Choice(
        interlocutor = Interlocutor.Guest(description = guestDescription),
        isSelected = form.interlocutor is Interlocutor.Guest,
        meta = "Только для этой темы",
        name = "Гость",
    )

    private fun DataState.formGuestDescription(): String? = (form.interlocutor as? Interlocutor.Guest)?.description

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
