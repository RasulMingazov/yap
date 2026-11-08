package app.yap.admin.feature.levels.presentation

import app.yap.admin.core.presentation.Status
import app.yap.admin.feature.levels.domain.Idioms
import app.yap.admin.feature.levels.domain.Level
import app.yap.admin.feature.levels.domain.LevelCode
import app.yap.admin.feature.levels.domain.RatingRange
import app.yap.admin.feature.levels.presentation.LevelsViewModel.DataState
import app.yap.admin.feature.levels.presentation.LevelsViewModel.Load
import app.yap.admin.feature.levels.presentation.LevelsViewModel.SaveStatus
import app.yap.admin.feature.levels.presentation.LevelsViewModel.UiState

internal object LevelsUiStateMapper {

    fun map(state: DataState): UiState = when (state.load) {
        Load.FAILED -> UiState.Error(message = "Не удалось загрузить уровни")
        Load.LOADED -> state.toContent()
        Load.LOADING -> UiState.Loading
    }

    private fun DataState.toContent(): UiState.Content = UiState.Content(
        block = block(),
        idioms = idiomsOptions(),
        isSaveEnabled = !isSaving,
        rows = rows(),
        saveStatus = saveStatus?.status(),
    )

    private fun DataState.block(): UiState.Block {
        val range = levels
            .first { level -> level.code == chosen }
            .range
        return UiState.Block(
            rating = range.rating(),
            text = forms.getValue(chosen).block,
            title = "Блок промпта · ${chosen.label()}",
        )
    }

    private fun RatingRange?.rating(): String? = if (this == null) null else "рейтинг ${text()}"

    private fun idiomsOptions(): List<UiState.IdiomsOption> = Idioms.entries.map { idioms ->
        UiState.IdiomsOption(
            idioms = idioms,
            label = idioms.label(),
        )
    }

    private fun Idioms.label(): String = when (this) {
        Idioms.FREELY -> "да"
        Idioms.NEVER -> "нет"
        Idioms.RARELY -> "редко"
        Idioms.SOMETIMES -> "иногда"
    }

    private fun DataState.rows(): List<UiState.Row> = levels.map { level -> row(level) }

    private fun DataState.row(level: Level): UiState.Row = UiState.Row(
        code = level.code,
        form = forms.getValue(level.code),
        isChosen = level.code == chosen,
        label = level.code.label(),
        range = level.range?.text(),
    )

    private fun RatingRange.text(): String = if (to == null) "$from+" else "$from–$to"

    private fun SaveStatus.status(): Status = when (this) {
        is SaveStatus.Invalid -> Status(
            text = "Заполните все уровни, рейтинг должен расти от A1 к C2",
            tone = Status.Tone.ERROR,
        )
        is SaveStatus.Saved -> Status(text = "Сохранено", tone = Status.Tone.SUCCESS)
        is SaveStatus.UnreadNumber -> Status(text = "Проверьте числа: ${code.label()}", tone = Status.Tone.ERROR)
    }

    private fun LevelCode.label(): String = if (this == LevelCode.NO_RATING) "Без рейтинга" else name
}
