package app.yap.admin.feature.levels.presentation

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
    fun `GIVEN levels before a save WHEN mapping THEN a row has no range and an empty form`() {
        val state = DataState().loaded(listOf(StubLevel.stubLevel(range = null, settings = null)))

        val uiState = LevelsUiStateMapper.map(state) as UiState.Content

        assertEquals(
            expected = UiState.Row(
                code = LevelCode.B1,
                form = LevelForm.EMPTY,
                isChosen = true,
                label = "B1",
                range = null,
            ),
            actual = uiState.rows.single(),
        )
    }

    @Test
    fun `GIVEN saved levels WHEN mapping THEN C2 has an open end and No rating has no range`() {
        val state = DataState().loaded(
            listOf(
                StubLevel.stubLevel(code = LevelCode.B1),
                StubLevel.stubLevel(code = LevelCode.C2, range = StubLevel.stubRange(from = 1500, to = null)),
                StubLevel.stubLevel(code = LevelCode.NO_RATING, range = null),
            ),
        )

        val uiState = LevelsUiStateMapper.map(state) as UiState.Content

        assertEquals(
            expected = listOf("B1" to "1050–1199", "C2" to "1500+", "Без рейтинга" to null),
            actual = uiState.rows.map { row -> row.label to row.range },
        )
    }

    @Test
    fun `GIVEN B1 is chosen WHEN mapping THEN its prompt block shows with its rating`() {
        val state = DataState().loaded(StubLevel.stubLevels())

        val uiState = LevelsUiStateMapper.map(state) as UiState.Content

        assertEquals(
            expected = UiState.Block(rating = "рейтинг 1050–1199", text = StubLevel.BLOCK, title = "Блок промпта · B1"),
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
    fun `GIVEN the levels did not load WHEN mapping THEN Levels shows the load error`() {
        val state = DataState(load = Load.FAILED)

        val uiState = LevelsUiStateMapper.map(state)

        assertEquals(expected = UiState.Error(message = "Не удалось загрузить уровни"), actual = uiState)
    }
}
