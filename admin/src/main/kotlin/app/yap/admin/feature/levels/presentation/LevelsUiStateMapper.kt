package app.yap.admin.feature.levels.presentation

import app.yap.admin.core.presentation.Status
import app.yap.admin.feature.levels.domain.Idioms
import app.yap.admin.feature.levels.domain.Level
import app.yap.admin.feature.levels.domain.LevelCode
import app.yap.admin.feature.levels.presentation.LevelsViewModel.DataState
import app.yap.admin.feature.levels.presentation.LevelsViewModel.Load
import app.yap.admin.feature.levels.presentation.LevelsViewModel.NumberField
import app.yap.admin.feature.levels.presentation.LevelsViewModel.SaveStatus
import app.yap.admin.feature.levels.presentation.LevelsViewModel.UiState

internal object LevelsUiStateMapper : (DataState) -> UiState {

    override fun invoke(state: DataState): UiState = when (state.load) {
        Load.FAILED -> UiState.Error(message = "Не удалось загрузить уровни")
        Load.LOADED -> state.toContent()
        Load.LOADING -> UiState.Loading
    }

    private fun DataState.toContent(): UiState.Content = UiState.Content(
        block = block(),
        idioms = Idioms.entries.map { idioms -> idioms.option() },
        isSaveEnabled = !isSaving,
        rows = levels.map { level -> row(level) },
        saveStatus = saveStatus(),
    )

    private fun DataState.block(): UiState.Block {
        val level = levels.first { level -> level.code == chosen }
        return UiState.Block(
            code = chosen,
            form = forms.getValue(chosen),
            rating = level.rating(),
            title = "Блок промпта · ${chosen.label()}",
        )
    }

    private fun Level.rating(): String? = rangeText()?.let { text -> "рейтинг $text" }

    private fun DataState.row(level: Level): UiState.Row {
        val form = forms.getValue(level.code)
        val label = level.code.label()
        return UiState.Row(
            code = level.code,
            form = form,
            idioms = form.idioms?.option(),
            isChosen = level.code == chosen,
            isRatingFromShown = level.code.hasRatingFrom,
            label = label,
            maxWordsLabel = "Слов в фразе $label",
            range = level.rangeText(),
            ratingFromLabel = "Рейтинг от $label",
            vocabularyLabel = "Лексика $label",
        )
    }

    private fun Idioms.option(): UiState.IdiomsOption = UiState.IdiomsOption(
        idioms = this,
        label = label(),
    )

    private fun Idioms.label(): String = when (this) {
        Idioms.FREELY -> "да"
        Idioms.NEVER -> "нет"
        Idioms.RARELY -> "редко"
        Idioms.SOMETIMES -> "иногда"
    }

    private fun Level.rangeText(): String? = when {
        !code.hasRatingFrom -> "и калибровка"
        range == null -> null
        range.to == null -> "${range.from}+"
        else -> "${range.from}–${range.to}"
    }

    private fun DataState.saveStatus(): Status? =
        if (isSaving) Status(text = "Сохранение…", tone = Status.Tone.MUTED) else saveStatus?.status()

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
