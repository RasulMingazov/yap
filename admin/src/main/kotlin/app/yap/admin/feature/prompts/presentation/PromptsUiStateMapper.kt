package app.yap.admin.feature.prompts.presentation

import app.yap.admin.core.presentation.Status
import app.yap.admin.feature.prompts.domain.Prompt
import app.yap.admin.feature.prompts.domain.PromptGroup
import app.yap.admin.feature.prompts.domain.PromptKey
import app.yap.admin.feature.prompts.domain.PromptSummary
import app.yap.admin.feature.prompts.domain.PromptVersion
import app.yap.admin.feature.prompts.presentation.PromptsViewModel.DataState
import app.yap.admin.feature.prompts.presentation.PromptsViewModel.Load
import app.yap.admin.feature.prompts.presentation.PromptsViewModel.SaveStatus
import app.yap.admin.feature.prompts.presentation.PromptsViewModel.UiState
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal object PromptsUiStateMapper {

    private val MONTHS = listOf("янв", "фев", "мар", "апр", "мая", "июн", "июл", "авг", "сен", "окт", "ноя", "дек")
    private val TIME = DateTimeFormatter.ofPattern("HH:mm")

    fun map(state: DataState): UiState = when (state.load) {
        Load.FAILED -> UiState.Error(message = "Не удалось загрузить промпты")
        Load.LOADED -> state.toContent()
        Load.LOADING -> UiState.Loading
    }

    private fun DataState.toContent(): UiState.Content {
        val prompt = prompts.getValue(chosen)
        return UiState.Content(
            groups = groups(),
            isSaveEnabled = !isSaving,
            saveStatus = saveStatus?.status(),
            state = prompt.state(),
            text = text,
            title = chosen.title(),
            variables = prompt.variables,
            versions = prompt.versions.map { version -> version.row() },
        )
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

    private fun SaveStatus.status(): Status = when (this) {
        SaveStatus.EMPTY_TEXT -> Status(text = "Введите текст промпта", tone = Status.Tone.ERROR)
        SaveStatus.SAVED -> Status(text = "Сохранено", tone = Status.Tone.SUCCESS)
    }

    private fun Prompt.state(): String {
        val current = versions.firstOrNull { version -> version.isCurrent }
        return if (current == null) "Ещё не сохранён" else "v${current.number} у пользователей"
    }

    private fun PromptVersion.row(): UiState.Version = UiState.Version(
        isCurrent = isCurrent,
        label = "v$number",
        number = number,
        savedAt = time(),
        state = state(),
    )

    private fun PromptVersion.time(): String {
        val time = savedAt.atZone(ZoneId.systemDefault())
        val month = MONTHS[time.monthValue - 1]
        return "${time.dayOfMonth} $month, ${TIME.format(time)}"
    }

    private fun PromptVersion.state(): String = if (isCurrent) "у пользователей" else "архив"
}
