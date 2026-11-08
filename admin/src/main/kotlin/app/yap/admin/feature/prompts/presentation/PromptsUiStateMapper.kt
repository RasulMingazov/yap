package app.yap.admin.feature.prompts.presentation

import app.yap.admin.core.presentation.Status
import app.yap.admin.core.presentation.shortName
import app.yap.admin.feature.prompts.domain.Prompt
import app.yap.admin.feature.prompts.domain.PromptGroup
import app.yap.admin.feature.prompts.domain.PromptKey
import app.yap.admin.feature.prompts.domain.PromptSummary
import app.yap.admin.feature.prompts.domain.PromptVersion
import app.yap.admin.feature.prompts.presentation.PromptsViewModel.DataState
import app.yap.admin.feature.prompts.presentation.PromptsViewModel.Load
import app.yap.admin.feature.prompts.presentation.PromptsViewModel.SendStatus
import app.yap.admin.feature.prompts.presentation.PromptsViewModel.UiState
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal object PromptsUiStateMapper : (DataState) -> UiState {

    private val TIME = DateTimeFormatter.ofPattern("HH:mm")

    override fun invoke(state: DataState): UiState = when (state.load) {
        Load.FAILED -> UiState.Error(message = "Не удалось загрузить промпты")
        Load.LOADED -> state.toContent()
        Load.LOADING -> UiState.Loading
    }

    private fun DataState.toContent(): UiState.Content = UiState.Content(
        editor = editor(),
        groups = groups(),
        isListEnabled = !isSending && promptLoad != Load.LOADING,
        isSaveEnabled = !isSending && promptLoad == Load.LOADED,
        saveStatus = saveStatus(),
    )

    private fun DataState.editor(): UiState.Editor = when (promptLoad) {
        Load.FAILED -> UiState.Editor.Error(message = "Не удалось загрузить промпт")
        Load.LOADED -> toEditor()
        Load.LOADING -> UiState.Editor.Loading
    }

    private fun DataState.toEditor(): UiState.Editor.Loaded {
        val prompt = prompts.getValue(chosen)
        return UiState.Editor.Loaded(
            isEditable = !isSending,
            isTextError = sendStatus is SendStatus.EmptyText,
            state = prompt.state(),
            text = text,
            title = chosen.title(),
            variables = prompt.variables,
            versions = prompt.versions.map { version -> version.row() },
            versionsEmpty = prompt.versionsEmpty(),
        )
    }

    private fun Prompt.state(): String {
        val current = versions.firstOrNull { version -> version.isCurrent }
        return if (current == null) "Ещё не сохранён" else "v${current.number} у пользователей"
    }

    private fun PromptVersion.row(): UiState.Version = UiState.Version(
        isRestoreShown = !isCurrent,
        label = "v$number",
        number = number,
        savedAt = time(),
        state = state(),
    )

    private fun PromptVersion.time(): String {
        val time = savedAt.atZone(ZoneId.systemDefault())
        return "${time.dayOfMonth} ${time.month.shortName()}, ${TIME.format(time)}"
    }

    private fun PromptVersion.state(): Status = if (isCurrent) {
        Status(text = "у пользователей", tone = Status.Tone.SUCCESS)
    } else {
        Status(text = "архив", tone = Status.Tone.MUTED)
    }

    private fun Prompt.versionsEmpty(): String? = if (versions.isEmpty()) {
        "Версий пока нет. Первая появится после «Сохранить»"
    } else {
        null
    }

    private fun DataState.groups(): List<UiState.Group> = summaries
        .groupBy { summary -> summary.group }
        .map { (group, summaries) -> group(group, summaries) }

    private fun DataState.group(group: PromptGroup, summaries: List<PromptSummary>): UiState.Group = UiState.Group(
        items = summaries.map { summary -> item(summary.promptKey) },
        title = group.title(),
    )

    private fun DataState.item(promptKey: PromptKey): UiState.Item = UiState.Item(
        isChosen = promptKey == chosen,
        promptKey = promptKey,
        title = promptKey.title(),
        usage = promptKey.usage(),
    )

    private fun DataState.saveStatus(): Status = when (val status = sendStatus) {
        is SendStatus.EmptyText -> Status(text = "Введите текст промпта", tone = Status.Tone.ERROR)
        is SendStatus.Idle -> Status(text = "Новая версия сразу уйдёт пользователям", tone = Status.Tone.MUTED)
        is SendStatus.Restored -> Status(text = "v${status.number} снова у пользователей", tone = Status.Tone.SUCCESS)
        is SendStatus.Restoring -> Status(text = "Возврат версии…", tone = Status.Tone.MUTED)
        is SendStatus.Saved -> Status(text = "Сохранено", tone = Status.Tone.SUCCESS)
        is SendStatus.Saving -> Status(text = "Сохранение…", tone = Status.Tone.MUTED)
    }
}
