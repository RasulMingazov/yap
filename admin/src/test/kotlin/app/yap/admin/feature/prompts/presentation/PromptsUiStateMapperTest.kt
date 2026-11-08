package app.yap.admin.feature.prompts.presentation

import app.yap.admin.core.presentation.Status
import app.yap.admin.feature.prompts.domain.Prompt
import app.yap.admin.feature.prompts.domain.SavePromptResult
import app.yap.admin.feature.prompts.domain.StubPrompt
import app.yap.admin.feature.prompts.presentation.PromptsViewModel.DataState
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

        val uiState = PromptsUiStateMapper(state) as UiState.Content

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

        val editor = editor(state)

        assertEquals(
            expected = Triple("", "Ещё не сохранён", emptyList()),
            actual = Triple(editor.text, editor.state, editor.versions),
        )
    }

    @Test
    fun `GIVEN a prompt without a version WHEN mapping THEN Versions says the first comes after Save`() {
        val state = loaded(StubPrompt.stubPrompt(text = null, versions = emptyList()))

        val editor = editor(state)

        assertEquals(expected = "Версий пока нет. Первая появится после «Сохранить»", actual = editor.versionsEmptyMessage)
    }

    @Test
    fun `GIVEN the versions 2 and 1 WHEN mapping THEN version 2 is live and version 1 is in the archive`() {
        val current = StubPrompt.stubPromptVersion(number = 2, savedAt = localTime(11, 8, 13))
        val older = StubPrompt.stubPromptVersion(isCurrent = false, savedAt = localTime(10, 20, 9))
        val state = loaded(StubPrompt.stubPrompt(versions = listOf(current, older)))

        val editor = editor(state)

        assertEquals(
            expected = "v2 у пользователей" to listOf(
                listOf("v2", "8 ноя, 13:05", "у пользователей", "false"),
                listOf("v1", "20 окт, 09:05", "архив", "true"),
            ),
            actual = editor.state to editor.versions.map { version -> version.row() },
        )
    }

    @Test
    fun `GIVEN a typed text WHEN mapping THEN the editor shows the typed text`() {
        val state = loaded().copy(texts = mapOf(StubPrompt.stubPrompt().promptKey to "Typed text"))

        val editor = editor(state)

        assertEquals(expected = "Typed text", actual = editor.text)
    }

    @Test
    fun `GIVEN no request WHEN mapping THEN the status says a new version goes to the users at once`() {
        val state = loaded()

        val uiState = PromptsUiStateMapper(state) as UiState.Content

        assertEquals(
            expected = Status(text = "Новая версия сразу уйдёт пользователям", tone = Status.Tone.MUTED),
            actual = uiState.saveStatus,
        )
    }

    @Test
    fun `GIVEN a save in progress WHEN mapping THEN the status says the prompt is saving`() {
        val state = loaded().copy(isSaving = true)

        val uiState = PromptsUiStateMapper(state) as UiState.Content

        assertEquals(expected = Status(text = "Сохранение…", tone = Status.Tone.MUTED), actual = uiState.saveStatus)
    }

    @Test
    fun `GIVEN a restore in progress WHEN mapping THEN the status says the version is restoring`() {
        val state = loaded().copy(isRestoring = true)

        val uiState = PromptsUiStateMapper(state) as UiState.Content

        assertEquals(expected = Status(text = "Возврат версии…", tone = Status.Tone.MUTED), actual = uiState.saveStatus)
    }

    @Test
    fun `GIVEN a request in progress WHEN mapping THEN the editor, the list and Save are disabled`() {
        val state = loaded().copy(isSaving = true)

        val uiState = PromptsUiStateMapper(state) as UiState.Content

        assertEquals(
            expected = Triple(false, false, false),
            actual = Triple(editor(state).isEditable, uiState.isListEnabled, uiState.isSaveEnabled),
        )
    }

    @Test
    fun `GIVEN a restore in progress WHEN mapping THEN the editor, the list and Restore are disabled`() {
        val state = loaded().copy(isRestoring = true)

        val uiState = PromptsUiStateMapper(state) as UiState.Content

        assertEquals(
            expected = Triple(false, false, false),
            actual = Triple(editor(state).isEditable, uiState.isListEnabled, editor(state).isRestoreEnabled),
        )
    }

    @Test
    fun `GIVEN a save WHEN mapping THEN the Saved status shows beside Save`() {
        val state = loaded().copy(saveResult = SavePromptResult.Saved(StubPrompt.stubPrompt()))

        val uiState = PromptsUiStateMapper(state) as UiState.Content

        assertEquals(expected = "Сохранено", actual = uiState.saveStatus.text)
    }

    @Test
    fun `GIVEN the server rejects an empty text WHEN mapping THEN the status and the editor show the error`() {
        val state = loaded().copy(saveResult = SavePromptResult.Rejected)

        val uiState = PromptsUiStateMapper(state) as UiState.Content

        assertEquals(
            expected = "Введите текст промпта" to true,
            actual = uiState.saveStatus.text to editor(state).isTextError,
        )
    }

    @Test
    fun `GIVEN a prompt loads WHEN mapping THEN the editor shows the loader and the list is disabled`() {
        val state = loaded().copy(isPromptLoading = true)

        val uiState = PromptsUiStateMapper(state) as UiState.Content

        assertEquals(
            expected = Triple(UiState.Editor.Loading, false, false),
            actual = Triple(uiState.editor, uiState.isListEnabled, uiState.isSaveEnabled),
        )
    }

    @Test
    fun `GIVEN the prompts did not load WHEN mapping THEN Prompts shows the load error`() {
        val state = DataState(isListLoadFailed = true)

        val uiState = PromptsUiStateMapper(state)

        assertEquals(expected = UiState.Error(message = "Не удалось загрузить промпты"), actual = uiState)
    }

    @Test
    fun `GIVEN a shown list WHEN the list loads again THEN Prompts shows the loader`() {
        val state = loaded().copy(isListLoading = true)

        val uiState = PromptsUiStateMapper(state)

        assertEquals(expected = UiState.Loading, actual = uiState)
    }

    @Test
    fun `GIVEN a prompt did not load WHEN mapping THEN the editor shows the error and Save is disabled`() {
        val state = loaded().copy(isPromptLoadFailed = true)

        val uiState = PromptsUiStateMapper(state) as UiState.Content

        assertEquals(
            expected = UiState.Editor.Error(message = "Не удалось загрузить промпт") to false,
            actual = uiState.editor to uiState.isSaveEnabled,
        )
    }

    @Test
    fun `GIVEN the server does not answer a save WHEN mapping THEN the status shows the note again`() {
        val state = loaded().copy(saveResult = SavePromptResult.Failed)

        val uiState = PromptsUiStateMapper(state) as UiState.Content

        assertEquals(expected = "Новая версия сразу уйдёт пользователям", actual = uiState.saveStatus.text)
    }

    private fun loaded(prompt: Prompt = StubPrompt.stubPrompt()): DataState = DataState(
        prompts = mapOf(prompt.promptKey to prompt),
        summaries = emptyList(),
        texts = mapOf(prompt.promptKey to prompt.text.orEmpty()),
    )

    private fun editor(state: DataState): UiState.Editor.Loaded {
        val content = PromptsUiStateMapper(state) as UiState.Content
        return content.editor as UiState.Editor.Loaded
    }

    private fun localTime(month: Int, day: Int, hour: Int): Instant = LocalDateTime
        .of(2026, month, day, hour, 5)
        .atZone(ZoneId.systemDefault())
        .toInstant()

    private fun UiState.Version.row(): List<String> = listOf(label, savedAt, state.text, isRestoreShown.toString())
}
