package app.yap.admin.feature.levels.presentation

import app.yap.admin.core.presentation.Status
import app.yap.admin.feature.levels.domain.Idioms
import app.yap.admin.feature.levels.domain.Level
import app.yap.admin.feature.levels.domain.LevelCode
import app.yap.admin.feature.levels.domain.SaveLevelsResult
import app.yap.admin.feature.levels.domain.StubLevel
import app.yap.admin.feature.levels.presentation.LevelsViewModel.DataState
import app.yap.admin.feature.levels.presentation.LevelsViewModel.UiState
import kotlin.test.Test
import kotlin.test.assertEquals

internal class LevelsUiStateMapperTest {

    @Test
    fun `GIVEN levels before a save WHEN mapping THEN a row has no range, an empty form and no idioms`() {
        val state = loaded(StubLevel.stubLevels(range = null, settings = null))

        val uiState = LevelsUiStateMapper(state) as UiState.Content

        assertEquals(
            expected = UiState.Row(
                code = LevelCode.B1,
                form = StubLevel.stubInput(block = "", idioms = null, maxWords = "", ratingFrom = "", vocabulary = ""),
                idioms = null,
                isChosen = true,
                isRatingFromShown = true,
                label = "B1",
                maxWordsLabel = "Слов в фразе B1",
                range = null,
                ratingFromLabel = "Рейтинг от B1",
                vocabularyLabel = "Лексика B1",
            ),
            actual = uiState.rows[LevelCode.B1.ordinal],
        )
    }

    @Test
    fun `GIVEN saved levels WHEN mapping THEN C2 has an open end`() {
        val state = loaded(StubLevel.stubLevels(range = StubLevel.stubRange(from = 1500, to = null)))

        val uiState = LevelsUiStateMapper(state) as UiState.Content

        assertEquals(expected = "1500+", actual = uiState.rows[LevelCode.C2.ordinal].range)
    }

    @Test
    fun `GIVEN No rating WHEN mapping THEN its row has no rating from and shows the calibration`() {
        val state = loaded(StubLevel.stubLevels(range = null))

        val uiState = LevelsUiStateMapper(state) as UiState.Content

        assertEquals(
            expected = Triple("Без рейтинга", "и калибровка", false),
            actual = uiState.rows.last().let { row -> Triple(row.label, row.range, row.isRatingFromShown) },
        )
    }

    @Test
    fun `GIVEN a saved level WHEN mapping THEN its idioms show as chosen`() {
        val state = loaded(StubLevel.stubLevels())

        val uiState = LevelsUiStateMapper(state) as UiState.Content

        assertEquals(
            expected = UiState.IdiomsOption(idioms = Idioms.RARELY, label = "редко"),
            actual = uiState.rows.first().idioms,
        )
    }

    @Test
    fun `GIVEN B1 is chosen WHEN mapping THEN its prompt block shows with its rating`() {
        val state = loaded(StubLevel.stubLevels())

        val uiState = LevelsUiStateMapper(state) as UiState.Content

        assertEquals(
            expected = UiState.Block(
                code = LevelCode.B1,
                form = StubLevel.stubInput(),
                rating = "рейтинг 1050–1199",
                title = "Блок промпта · B1",
            ),
            actual = uiState.block,
        )
    }

    @Test
    fun `GIVEN the levels WHEN mapping THEN the idioms show the names of the canvas`() {
        val state = loaded(StubLevel.stubLevels())

        val uiState = LevelsUiStateMapper(state) as UiState.Content

        assertEquals(
            expected = listOf("нет", "редко", "иногда", "да"),
            actual = uiState.idioms.map { option -> option.label },
        )
    }

    @Test
    fun `GIVEN a save in progress WHEN mapping THEN the saving status shows beside a disabled Save`() {
        val state = loaded(StubLevel.stubLevels())
            .copy(isSaving = true)

        val uiState = LevelsUiStateMapper(state) as UiState.Content

        assertEquals(
            expected = Status(text = "Сохранение…", tone = Status.Tone.MUTED) to false,
            actual = uiState.saveStatus to uiState.isSaveEnabled,
        )
    }

    @Test
    fun `GIVEN the levels did not load WHEN mapping THEN Levels shows the load error`() {
        val state = DataState(isLoadFailed = true)

        val uiState = LevelsUiStateMapper(state)

        assertEquals(expected = UiState.Error(message = "Не удалось загрузить уровни"), actual = uiState)
    }

    @Test
    fun `GIVEN a rating from that is not a number WHEN mapping THEN the status names the field and the level`() {
        val result = SaveLevelsResult.UnreadNumber(LevelCode.B2, SaveLevelsResult.NumberField.RATING_FROM)
        val state = loaded(StubLevel.stubLevels()).copy(saveResult = result)

        val uiState = LevelsUiStateMapper(state) as UiState.Content

        assertEquals(
            expected = Status(text = "Проверьте число в «Рейтинг»: B2", tone = Status.Tone.ERROR),
            actual = uiState.saveStatus,
        )
    }

    @Test
    fun `GIVEN No rating without idioms WHEN mapping THEN the status asks to choose its idioms`() {
        val state = loaded(StubLevel.stubLevels()).copy(saveResult = SaveLevelsResult.NoIdioms(LevelCode.NO_RATING))

        val uiState = LevelsUiStateMapper(state) as UiState.Content

        assertEquals(
            expected = Status(text = "Выберите идиомы: Без рейтинга", tone = Status.Tone.ERROR),
            actual = uiState.saveStatus,
        )
    }

    @Test
    fun `GIVEN B1 without a word count WHEN mapping THEN the status asks to type the number`() {
        val state = loaded(StubLevel.stubLevels()).copy(saveResult = SaveLevelsResult.NoWordCount(LevelCode.B1))

        val uiState = LevelsUiStateMapper(state) as UiState.Content

        assertEquals(
            expected = Status(text = "Введите число в «Слов в фразе»: B1", tone = Status.Tone.ERROR),
            actual = uiState.saveStatus,
        )
    }

    private fun loaded(levels: List<Level>): DataState = DataState(
        forms = levels.toInputs(),
        levels = levels,
    )
}
