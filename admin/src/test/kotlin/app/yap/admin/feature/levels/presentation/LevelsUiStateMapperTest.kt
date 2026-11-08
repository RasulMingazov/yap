package app.yap.admin.feature.levels.presentation

import app.yap.admin.core.presentation.Status
import app.yap.admin.feature.levels.domain.Idioms
import app.yap.admin.feature.levels.domain.LevelCode
import app.yap.admin.feature.levels.domain.StubLevel
import app.yap.admin.feature.levels.presentation.LevelsViewModel.DataState
import app.yap.admin.feature.levels.presentation.LevelsViewModel.LevelForm
import app.yap.admin.feature.levels.presentation.LevelsViewModel.Load
import app.yap.admin.feature.levels.presentation.LevelsViewModel.UiState
import kotlin.test.Test
import kotlin.test.assertEquals

internal class LevelsUiStateMapperTest {

    @Test
    fun `GIVEN levels before a save WHEN mapping THEN a row has no range, an empty form and no idioms`() {
        val state = DataState().loaded(StubLevel.stubLevels(range = null, settings = null))

        val uiState = LevelsUiStateMapper.map(state) as UiState.Content

        assertEquals(
            expected = UiState.Row(
                code = LevelCode.B1,
                form = LevelForm.EMPTY,
                idioms = null,
                isChosen = true,
                isRatingFromShown = true,
                label = "B1",
                range = null,
            ),
            actual = uiState.rows[LevelCode.B1.ordinal],
        )
    }

    @Test
    fun `GIVEN saved levels WHEN mapping THEN C2 has an open end`() {
        val state = DataState().loaded(StubLevel.stubLevels(range = StubLevel.stubRange(from = 1500, to = null)))

        val uiState = LevelsUiStateMapper.map(state) as UiState.Content

        assertEquals(expected = "1500+", actual = uiState.rows[LevelCode.C2.ordinal].range)
    }

    @Test
    fun `GIVEN No rating WHEN mapping THEN its row has no rating from and shows the calibration`() {
        val state = DataState().loaded(StubLevel.stubLevels(range = null))

        val uiState = LevelsUiStateMapper.map(state) as UiState.Content

        assertEquals(
            expected = Triple("Без рейтинга", "и калибровка", false),
            actual = uiState.rows.last().let { row -> Triple(row.label, row.range, row.isRatingFromShown) },
        )
    }

    @Test
    fun `GIVEN a saved level WHEN mapping THEN its idioms show as chosen`() {
        val state = DataState().loaded(StubLevel.stubLevels())

        val uiState = LevelsUiStateMapper.map(state) as UiState.Content

        assertEquals(
            expected = UiState.IdiomsOption(idioms = Idioms.RARELY, label = "редко"),
            actual = uiState.rows.first().idioms,
        )
    }

    @Test
    fun `GIVEN B1 is chosen WHEN mapping THEN its prompt block shows with its rating`() {
        val state = DataState().loaded(StubLevel.stubLevels())

        val uiState = LevelsUiStateMapper.map(state) as UiState.Content

        assertEquals(
            expected = UiState.Block(
                code = LevelCode.B1,
                form = LevelForm.of(StubLevel.stubSettings()),
                rating = "рейтинг 1050–1199",
                title = "Блок промпта · B1",
            ),
            actual = uiState.block,
        )
    }

    @Test
    fun `GIVEN the levels WHEN mapping THEN the idioms show the names of the canvas`() {
        val state = DataState().loaded(StubLevel.stubLevels())

        val uiState = LevelsUiStateMapper.map(state) as UiState.Content

        assertEquals(
            expected = listOf("нет", "редко", "иногда", "да"),
            actual = uiState.idioms.map { option -> option.label },
        )
    }

    @Test
    fun `GIVEN a save in progress WHEN mapping THEN the saving status shows beside a disabled Save`() {
        val state = DataState()
            .loaded(StubLevel.stubLevels())
            .copy(isSaving = true)

        val uiState = LevelsUiStateMapper.map(state) as UiState.Content

        assertEquals(
            expected = Status(text = "Сохранение…", tone = Status.Tone.MUTED) to false,
            actual = uiState.saveStatus to uiState.isSaveEnabled,
        )
    }

    @Test
    fun `GIVEN the levels did not load WHEN mapping THEN Levels shows the load error`() {
        val state = DataState(load = Load.FAILED)

        val uiState = LevelsUiStateMapper.map(state)

        assertEquals(expected = UiState.Error(message = "Не удалось загрузить уровни"), actual = uiState)
    }
}
