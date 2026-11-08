package app.yap.admin.feature.topics.presentation

import app.yap.admin.feature.characters.domain.Character
import app.yap.admin.feature.topics.domain.Interlocutor
import app.yap.admin.feature.topics.presentation.TopicViewModel.DataState
import app.yap.admin.feature.topics.presentation.TopicViewModel.UiState.Content.Choice

internal object InterlocutorUiStateMapper {

    fun castNote(state: DataState): String? =
        if (state.cast.isEmpty()) "Персонажей пока нет. Добавьте их в разделе «Персонажи»" else null

    fun choices(state: DataState): List<Choice> {
        val castChoices = state.cast.map { character -> character.toChoice(state.form.interlocutor) }
        return castChoices + state.guestChoice()
    }

    fun guestDescription(state: DataState): String? = (state.form.interlocutor as? Interlocutor.Guest)?.description

    private fun Character.toChoice(selected: Interlocutor?): Choice {
        val interlocutor = Interlocutor.Cast(characterId = characterId)
        return Choice(
            interlocutor = interlocutor,
            isSelected = interlocutor == selected,
            meta = null,
            name = profile.name,
        )
    }

    private fun DataState.guestChoice(): Choice = Choice(
        interlocutor = Interlocutor.Guest(description = guestDescription),
        isSelected = form.interlocutor is Interlocutor.Guest,
        meta = "Только для этой темы",
        name = "Гость",
    )
}
