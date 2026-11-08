package app.yap.admin.feature.levels.presentation

import app.yap.admin.core.presentation.Status
import app.yap.admin.feature.levels.domain.Idioms
import app.yap.admin.feature.levels.domain.Level
import app.yap.admin.feature.levels.domain.LevelCode
import app.yap.admin.feature.levels.domain.RatingRange
import app.yap.admin.feature.levels.presentation.LevelsViewModel.DataState
import app.yap.admin.feature.levels.presentation.LevelsViewModel.Load
import app.yap.admin.feature.levels.presentation.LevelsViewModel.NumberField
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
        idioms = Idioms.entries.map(::idiomsOption),
        isSaveEnabled = !isSaving,
        rows = levels.map { level -> row(level) },
        saveStatus = saveStatus?.status(),
    )

    private fun DataState.block(): UiState.Block {
        val range = levels
            .first { level -> level.code == chosen }
            .range
        return UiState.Block(
            rating = range?.let { chosenRange -> "рейтинг ${chosenRange.text()}" },
            text = forms.getValue(chosen).block,
            title = "Блок промпта · ${chosen.label()}",
        )
    }

    private fun DataState.row(level: Level): UiState.Row {
        val form = forms.getValue(level.code)
        return UiState.Row(
            code = level.code,
            form = form,
            idioms = form.idioms?.let(::idiomsOption),
            isChosen = level.code == chosen,
            isRatingFromShown = level.code.hasRatingFrom,
            label = level.code.label(),
            range = level.rangeText(),
        )
    }

    private fun idiomsOption(idioms: Idioms): UiState.IdiomsOption = UiState.IdiomsOption(
        idioms = idioms,
        label = idioms.label(),
    )

    private fun Idioms.label(): String = when (this) {
        Idioms.FREELY -> "да"
        Idioms.NEVER -> "нет"
        Idioms.RARELY -> "редко"
        Idioms.SOMETIMES -> "иногда"
    }

    private fun Level.rangeText(): String? = when {
        !code.hasRatingFrom -> "и калибровка"
        else -> range?.text()
    }

    private fun RatingRange.text(): String = if (to == null) "$from+" else "$from–$to"

    private fun SaveStatus.status(): Status = when (this) {
        is SaveStatus.Invalid -> Status(
            text = "Заполните все уровни, рейтинг должен расти от A1 к C2",
            tone = Status.Tone.ERROR,
        )
        is SaveStatus.Saved -> Status(text = "Сохранено", tone = Status.Tone.SUCCESS)
        is SaveStatus.UnreadNumber -> Status(
            text = "Проверьте число в «${field.label()}»: ${code.label()}",
            tone = Status.Tone.ERROR,
        )
    }

    private fun NumberField.label(): String = when (this) {
        NumberField.MAX_WORDS -> "Слов в фразе"
        NumberField.RATING_FROM -> "Рейтинг"
    }

    private fun LevelCode.label(): String = if (this == LevelCode.NO_RATING) "Без рейтинга" else name
}
