package app.yap.admin.feature.levels.presentation

import app.yap.admin.core.presentation.Status
import app.yap.admin.feature.levels.domain.Idioms
import app.yap.admin.feature.levels.domain.Level
import app.yap.admin.feature.levels.domain.LevelCode
import app.yap.admin.feature.levels.domain.SaveLevelsResult
import app.yap.admin.feature.levels.domain.SaveLevelsResult.NumberField
import app.yap.admin.feature.levels.domain.hasEveryLevel
import app.yap.admin.feature.levels.presentation.LevelsViewModel.DataState
import app.yap.admin.feature.levels.presentation.LevelsViewModel.UiState

internal object LevelsUiStateMapper : (DataState) -> UiState {

    private const val LOAD_ERROR = "Не удалось загрузить уровни"

    override fun invoke(state: DataState): UiState {
        val levels = state.levels
        return when {
            state.isLoadFailed -> UiState.Error(message = LOAD_ERROR)
            levels == null -> UiState.Loading
            !levels.hasEveryLevel() -> UiState.Error(message = LOAD_ERROR)
            else -> state.toContent(levels)
        }
    }

    private fun DataState.toContent(levels: List<Level>): UiState.Content = UiState.Content(
        block = block(levels),
        idioms = Idioms.entries.map { idioms -> idioms.option() },
        isSaveEnabled = !isSaving,
        rows = levels.map { level -> row(level) },
        saveStatus = saveStatus(),
    )

    private fun DataState.block(levels: List<Level>): UiState.Block {
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
        if (isSaving) Status(text = "Сохранение…", tone = Status.Tone.MUTED) else saveResult?.status()

    private fun SaveLevelsResult.status(): Status? = when (this) {
        is SaveLevelsResult.Failed -> null
        is SaveLevelsResult.Invalid -> Status(
            text = "Сервер не принял уровни: заполните все поля, числа больше нуля, рейтинг растёт от A1 к C2",
            tone = Status.Tone.ERROR,
        )
        is SaveLevelsResult.NoIdioms -> Status(text = "Выберите идиомы: ${code.label()}", tone = Status.Tone.ERROR)
        is SaveLevelsResult.Saved -> Status(text = "Сохранено", tone = Status.Tone.SUCCESS)
        is SaveLevelsResult.UnreadNumber -> Status(
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
