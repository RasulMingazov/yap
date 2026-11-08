package app.yap.admin.feature.topics.presentation

import app.yap.admin.core.presentation.Status
import app.yap.admin.feature.characters.domain.CharacterId
import app.yap.admin.feature.characters.domain.StubCharacter
import app.yap.admin.feature.characters.domain.StubCharacterSummary
import app.yap.admin.feature.topics.domain.Interlocutor
import app.yap.admin.feature.topics.domain.StubTopic
import app.yap.admin.feature.topics.presentation.TopicViewModel.DataState
import app.yap.admin.feature.topics.presentation.TopicViewModel.Load
import app.yap.admin.feature.topics.presentation.TopicViewModel.SaveStatus
import app.yap.admin.feature.topics.presentation.TopicViewModel.UiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class TopicUiStateMapperTest {

    @Test
    fun `GIVEN a new topic WHEN mapping THEN the fields are empty, with one empty step and the cast to choose from`() {
        val state = DataState(cast = listOf(StubCharacterSummary.stubCharacterSummary()), load = Load.LOADED)

        val content = contentOf(state)

        assertEquals(expected = "Новая тема", actual = content.title)
        assertEquals(expected = "Ещё не сохранена", actual = content.subtitle)
        assertEquals(expected = null, actual = content.releaseDateText)
        assertEquals(
            expected = "Для черновика нужны название и дата выхода. Остальное можно заполнить позже.",
            actual = content.hint,
        )
        assertEquals(
            expected = listOf(StubTopic.stubStep(outcome = "", passWhen = "", task = "")),
            actual = content.form.steps,
        )
        assertEquals(expected = listOf("Sam", "Гость"), actual = content.choices.map { choice -> choice.name })
        assertEquals(expected = "Только для этой темы", actual = content.choices.last().meta)
        assertEquals(expected = null, actual = content.castNote)
    }

    @Test
    fun `GIVEN an empty cast WHEN mapping THEN a note above Guest says where to add characters`() {
        val content = contentOf(DataState(load = Load.LOADED))

        assertEquals(expected = "Персонажей пока нет. Добавьте их в разделе «Персонажи»", actual = content.castNote)
    }

    @Test
    fun `GIVEN characters with topics WHEN mapping THEN each cast row shows its topic count`() {
        val cast = listOf(
            StubCharacterSummary.stubCharacterSummary(topicCount = 12),
            StubCharacterSummary.stubCharacterSummary(
                characterId = MIA_ID,
                profile = StubCharacter.stubProfile(name = "Mia"),
                topicCount = 1,
            ),
        )

        val content = contentOf(DataState(cast = cast, load = Load.LOADED))

        assertEquals(
            expected = listOf("12 тем", "1 тема", "Только для этой темы"),
            actual = content.choices.map { choice -> choice.meta },
        )
    }

    @Test
    fun `GIVEN a saved topic WHEN mapping THEN the title, the long date and the draft subtitle come from the topic`() {
        val topic = StubTopic.stubTopic()
        val state = DataState(
            cast = listOf(StubCharacterSummary.stubCharacterSummary()),
            form = topic.content,
            load = Load.LOADED,
            saved = topic,
        )

        val content = contentOf(state)

        assertEquals(expected = "At the cafe", actual = content.title)
        assertEquals(expected = "Пятница, 24 октября · черновик", actual = content.subtitle)
        assertEquals(expected = "Пятница, 24 октября", actual = content.releaseDateText)
        assertEquals(expected = null, actual = content.hint)
        assertEquals(expected = null, actual = content.guestDescription)
        assertEquals(expected = listOf(true, false), actual = content.choices.map { choice -> choice.isSelected })
        assertEquals(
            expected = Interlocutor.Cast(characterId = CharacterId(StubCharacter.CHARACTER_ID)),
            actual = content.choices.first().interlocutor,
        )
    }

    @Test
    fun `GIVEN Guest is chosen WHEN mapping THEN the guest description shows and the Guest row is selected`() {
        val guest = Interlocutor.Guest(description = "A tired taxi driver.")
        val state = DataState(form = StubTopic.stubContent(interlocutor = guest), load = Load.LOADED)

        val content = contentOf(state)

        assertEquals(expected = "A tired taxi driver.", actual = content.guestDescription)
        assertEquals(expected = true, actual = content.choices.single().isSelected)
    }

    @Test
    fun `GIVEN the server rejected the draft WHEN mapping THEN one error names the four fields`() {
        val state = DataState(load = Load.LOADED, saveStatus = SaveStatus.REJECTED)

        val content = contentOf(state)

        assertEquals(
            expected = Status(
                text = "Сервер не принял черновик: проверьте название, дату, длительность и собеседника",
                tone = Status.Tone.ERROR,
            ),
            actual = content.saveStatus,
        )
    }

    @Test
    fun `GIVEN the topic is saved WHEN mapping THEN the status says Saved`() {
        val state = DataState(load = Load.LOADED, saveStatus = SaveStatus.SAVED)

        val content = contentOf(state)

        assertEquals(expected = Status(text = "Сохранено", tone = Status.Tone.SUCCESS), actual = content.saveStatus)
    }

    @Test
    fun `GIVEN a save in progress WHEN mapping THEN Save draft is disabled and the status says Saving`() {
        val state = DataState(isSaving = true, load = Load.LOADED)

        val content = contentOf(state)

        val saving = Status(text = "Сохранение…", tone = Status.Tone.MUTED)
        assertEquals(expected = false to saving, actual = content.isSaveEnabled to content.saveStatus)
    }

    @Test
    fun `GIVEN the topic or the cast did not load WHEN mapping THEN the screen shows the error`() {
        val state = DataState(load = Load.FAILED)

        val uiState = TopicUiStateMapper(state)

        assertEquals(expected = UiState.Error(message = "Не удалось загрузить тему"), actual = uiState)
    }

    private fun contentOf(state: DataState): UiState.Content = assertIs<UiState.Content>(TopicUiStateMapper(state))

    private companion object {

        const val MIA_ID = "7d1e0c2a-4b3f-4e6a-9c8d-2f1a0b3c4d5e"
    }
}
