package app.yap.admin.feature.prompts.presentation

import app.yap.admin.feature.prompts.domain.Prompt
import app.yap.admin.feature.prompts.domain.StubPrompt
import app.yap.admin.feature.prompts.presentation.PromptsViewModel.DataState
import app.yap.admin.feature.prompts.presentation.PromptsViewModel.Load
import app.yap.admin.feature.prompts.presentation.PromptsViewModel.SaveStatus
import app.yap.admin.feature.prompts.presentation.PromptsViewModel.UiState
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals

internal class PromptsUiStateMapperTest {

    @Test
    fun `GIVEN the six prompts WHEN mapping THEN the list shows them in the groups Topic and Calibration`() {
        val state = loaded().copy(summaries = StubPrompt.stubPromptSummaries())

        val uiState = PromptsUiStateMapper.map(state) as UiState.Content

        assertEquals(
            expected = listOf(
                "Тема" to listOf("Реплика персонажа", "Проверка шага", "Подсказки", "Разбор и рекомендации"),
                "Калибровка" to listOf("Проверка ответа", "Реплика Сэма"),
            ),
            actual = uiState.groups.map { group -> group.title to group.items.map { item -> item.title } },
        )
    }

    @Test
    fun `GIVEN a prompt without a version WHEN mapping THEN the editor is empty and not saved yet`() {
        val state = loaded(StubPrompt.stubPrompt(text = null, versions = emptyList()))

        val uiState = PromptsUiStateMapper.map(state) as UiState.Content

        assertEquals(
            expected = Triple("", "Ещё не сохранён", emptyList()),
            actual = Triple(uiState.text, uiState.state, uiState.versions),
        )
    }

    @Test
    fun `GIVEN the versions 2 and 1 WHEN mapping THEN version 2 is live and version 1 is in the archive`() {
        val current = StubPrompt.stubPromptVersion(number = 2, savedAt = localTime(11, 8, 13))
        val older = StubPrompt.stubPromptVersion(isCurrent = false, savedAt = localTime(10, 20, 9))
        val state = loaded(StubPrompt.stubPrompt(versions = listOf(current, older)))

        val uiState = PromptsUiStateMapper.map(state) as UiState.Content

        assertEquals(
            expected = "v2 у пользователей" to listOf(
                listOf("v2", "8 ноя, 13:05", "у пользователей"),
                listOf("v1", "20 окт, 09:05", "архив"),
            ),
            actual = uiState.state to uiState.versions.map { version -> version.row() },
        )
    }

    @Test
    fun `GIVEN a typed text WHEN mapping THEN the editor shows the typed text`() {
        val state = loaded().copy(texts = mapOf(StubPrompt.stubPrompt().promptKey to "Typed text"))

        val uiState = PromptsUiStateMapper.map(state) as UiState.Content

        assertEquals(expected = "Typed text", actual = uiState.text)
    }

    @Test
    fun `GIVEN a save WHEN mapping THEN the Saved status shows beside Save`() {
        val state = loaded().copy(saveStatus = SaveStatus.SAVED)

        val uiState = PromptsUiStateMapper.map(state) as UiState.Content

        assertEquals(expected = "Сохранено", actual = uiState.saveStatus?.text)
    }

    @Test
    fun `GIVEN the server rejects an empty text WHEN mapping THEN the status asks for the text`() {
        val state = loaded().copy(saveStatus = SaveStatus.EMPTY_TEXT)

        val uiState = PromptsUiStateMapper.map(state) as UiState.Content

        assertEquals(expected = "Введите текст промпта", actual = uiState.saveStatus?.text)
    }

    @Test
    fun `GIVEN the prompts did not load WHEN mapping THEN Prompts shows the load error`() {
        val state = DataState(load = Load.FAILED)

        val uiState = PromptsUiStateMapper.map(state)

        assertEquals(expected = UiState.Error(message = "Не удалось загрузить промпты"), actual = uiState)
    }

    private fun loaded(prompt: Prompt = StubPrompt.stubPrompt()): DataState = DataState(
        load = Load.LOADED,
        prompts = mapOf(prompt.promptKey to prompt),
    )

    private fun localTime(month: Int, day: Int, hour: Int): Instant = LocalDateTime
        .of(2026, month, day, hour, 5)
        .atZone(ZoneId.systemDefault())
        .toInstant()

    private fun UiState.Version.row(): List<String> = listOf(label, savedAt, state)
}
