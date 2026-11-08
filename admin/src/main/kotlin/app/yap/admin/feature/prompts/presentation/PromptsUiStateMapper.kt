package app.yap.admin.feature.prompts.presentation

import app.yap.admin.core.presentation.Status
import app.yap.admin.core.presentation.shortName
import app.yap.admin.feature.prompts.domain.Prompt
import app.yap.admin.feature.prompts.domain.PromptGroup
import app.yap.admin.feature.prompts.domain.PromptKey
import app.yap.admin.feature.prompts.domain.PromptSummary
import app.yap.admin.feature.prompts.domain.PromptVersion
import app.yap.admin.feature.prompts.domain.SavePromptResult
import app.yap.admin.feature.prompts.presentation.PromptsViewModel.DataState
import app.yap.admin.feature.prompts.presentation.PromptsViewModel.UiState
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal object PromptsUiStateMapper : (DataState) -> UiState {

    private val SAVE_NOTE = Status(text = "Новая версия сразу уйдёт пользователям", tone = Status.Tone.MUTED)
    private val TIME = DateTimeFormatter.ofPattern("HH:mm")

    override fun invoke(state: DataState): UiState {
        val summaries = state.summaries
        return when {
            state.isListLoadFailed -> UiState.Error(message = "Не удалось загрузить промпты")
            state.isListLoading || summaries == null -> UiState.Loading
            else -> state.toContent(summaries)
        }
    }

    private fun DataState.toContent(summaries: List<PromptSummary>): UiState.Content = UiState.Content(
        editor = editor(),
        groups = groups(summaries),
        isListEnabled = isListEnabled(),
        isSaveEnabled = isSaveEnabled(),
        saveStatus = saveStatus(),
    )

    private fun DataState.editor(): UiState.Editor = when {
        isPromptLoadFailed -> UiState.Editor.Error(message = "Не удалось загрузить промпт")
        isPromptLoading -> UiState.Editor.Loading
        else -> toEditor()
    }

    private fun DataState.toEditor(): UiState.Editor.Loaded {
        val prompt = prompts.getValue(chosen)
        return UiState.Editor.Loaded(
            isEditable = !isSending(),
            isRestoreEnabled = !isSending(),
            isTextError = saveResult is SavePromptResult.Rejected,
            state = prompt.state(),
            text = texts.getValue(chosen),
            title = chosen.title(),
            variables = prompt.variables,
            versions = prompt.versions.map { version -> version.row() },
            versionsEmptyMessage = prompt.versionsEmptyMessage(),
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

    private fun Prompt.versionsEmptyMessage(): String? = if (versions.isEmpty()) {
        "Версий пока нет. Первая появится после «Сохранить»"
    } else {
        null
    }

    private fun DataState.groups(summaries: List<PromptSummary>): List<UiState.Group> = summaries
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

    private fun DataState.isListEnabled(): Boolean = !isSending() && !isPromptLoading

    private fun DataState.isSaveEnabled(): Boolean = !isSending() && !isPromptLoading && !isPromptLoadFailed

    private fun DataState.isSending(): Boolean = isRestoring || isSaving

    private fun DataState.saveStatus(): Status {
        val saveResult = saveResult
        return when {
            isSaving -> Status(text = "Сохранение…", tone = Status.Tone.MUTED)
            isRestoring -> Status(text = "Возврат версии…", tone = Status.Tone.MUTED)
            restoredNumber != null -> Status(text = "v$restoredNumber снова у пользователей", tone = Status.Tone.SUCCESS)
            saveResult == null -> SAVE_NOTE
            else -> saveResult.status()
        }
    }

    private fun SavePromptResult.status(): Status = when (this) {
        is SavePromptResult.Failed -> SAVE_NOTE
        is SavePromptResult.Rejected -> Status(text = "Введите текст промпта", tone = Status.Tone.ERROR)
        is SavePromptResult.Saved -> Status(text = "Сохранено", tone = Status.Tone.SUCCESS)
    }
}
