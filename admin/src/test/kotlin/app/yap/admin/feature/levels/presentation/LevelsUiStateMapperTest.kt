package app.yap.admin.feature.levels.presentation

import app.yap.admin.core.presentation.Status
import app.yap.admin.feature.levels.domain.Idioms
import app.yap.admin.feature.levels.domain.Level
import app.yap.admin.feature.levels.domain.LevelCode
import app.yap.admin.feature.levels.domain.SaveLevelsResult
import app.yap.admin.feature.levels.domain.StubLevel
import app.yap.admin.feature.levels.presentation.LevelsViewModel.DataState
import app.yap.admin.feature.levels.presentation.LevelsViewModel.UiState
import app.yap.admin.feature.models.domain.StubModels
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
                model = DEFAULT_MODEL,
                range = null,
                ratingFromLabel = "Рейтинг от B1",
                ratingNote = null,
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
        val state = loaded(StubLevel.stubLevels())
            .copy(saveResult = result)

        val uiState = LevelsUiStateMapper(state) as UiState.Content

        assertEquals(
            expected = Status(text = "Проверьте число в «Рейтинг»: B2", tone = Status.Tone.ERROR),
            actual = uiState.saveStatus,
        )
    }

    @Test
    fun `GIVEN No rating without idioms WHEN mapping THEN the status asks to choose its idioms`() {
        val state = loaded(StubLevel.stubLevels())
            .copy(saveResult = SaveLevelsResult.NoIdioms(LevelCode.NO_RATING))

        val uiState = LevelsUiStateMapper(state) as UiState.Content

        assertEquals(
            expected = Status(text = "Выберите идиомы: Без рейтинга", tone = Status.Tone.ERROR),
            actual = uiState.saveStatus,
        )
    }

    @Test
    fun `GIVEN B1 without a word count WHEN mapping THEN the status asks to type the number`() {
        val state = loaded(StubLevel.stubLevels())
            .copy(saveResult = SaveLevelsResult.NoWordCount(LevelCode.B1))

        val uiState = LevelsUiStateMapper(state) as UiState.Content

        assertEquals(
            expected = Status(text = "Введите число в «Слов в фразе»: B1", tone = Status.Tone.ERROR) to false,
            actual = uiState.saveStatus to uiState.isRefreshShown,
        )
    }

    @Test
    fun `GIVEN levels before a save WHEN mapping THEN every Model shows Default`() {
        val state = loaded(StubLevel.stubLevels(settings = null))
            .copy(models = StubModels.stubModels())

        val uiState = LevelsUiStateMapper(state) as UiState.Content

        assertEquals(
            expected = List(LevelCode.entries.size) { DEFAULT_MODEL },
            actual = uiState.rows.map { row -> row.model },
        )
    }

    @Test
    fun `GIVEN saved levels with a model WHEN mapping THEN every row shows the model`() {
        val state = loaded(StubLevel.stubLevels(settings = StubLevel.stubSettings(model = PRO)))
            .copy(models = StubModels.stubModels())

        val uiState = LevelsUiStateMapper(state) as UiState.Content

        assertEquals(
            expected = List(LevelCode.entries.size) { PRO },
            actual = uiState.rows.map { row -> row.model.model },
        )
    }

    @Test
    fun `GIVEN the models still load WHEN mapping THEN the choice is disabled with no models error`() {
        val state = loaded(StubLevel.stubLevels())

        val uiState = LevelsUiStateMapper(state) as UiState.Content

        assertEquals(
            expected = false to null,
            actual = uiState.isModelChoiceEnabled to uiState.modelError,
        )
    }

    @Test
    fun `GIVEN the catalog WHEN mapping THEN Model offers Default once and the models in the server order`() {
        val other = StubModels.stubProvider(
            code = "OTHER",
            models = listOf("alpha-model"),
        )
        val state = loaded(StubLevel.stubLevels())
            .copy(models = StubModels.stubModels(providers = listOf(StubModels.stubProvider(), other)))

        val uiState = LevelsUiStateMapper(state) as UiState.Content

        assertEquals(
            expected = Triple(listOf("по умолчанию", "deepseek-flash", "deepseek-v4-pro", "alpha-model"), true, null),
            actual = Triple(uiState.models.map { option -> option.label }, uiState.isModelChoiceEnabled, uiState.modelError),
        )
    }

    @Test
    fun `GIVEN B1 with a model WHEN mapping THEN only its row shows the model`() {
        val levels = StubLevel.stubLevels()
        val state = loaded(levels)
            .copy(
                forms = levels.toInputs() + (LevelCode.B1 to StubLevel.stubInput(model = PRO)),
                models = StubModels.stubModels(),
            )

        val uiState = LevelsUiStateMapper(state) as UiState.Content

        assertEquals(
            expected = listOf(null, null, PRO, null, null, null, null),
            actual = uiState.rows.map { row -> row.model.model },
        )
    }

    @Test
    fun `GIVEN the levels WHEN mapping THEN only No rating says where the calibration model is set`() {
        val state = loaded(StubLevel.stubLevels())

        val uiState = LevelsUiStateMapper(state) as UiState.Content

        assertEquals(
            expected = List(LevelCode.entries.size - 1) { null } + "Модель калибровки задаётся в «Моделях»",
            actual = uiState.rows.map { row -> row.ratingNote },
        )
    }

    @Test
    fun `GIVEN the models did not load WHEN mapping THEN the table shows Default disabled and the models error`() {
        val levels = StubLevel.stubLevels()
        val state = loaded(levels)
            .copy(
                forms = levels.toInputs() + (LevelCode.B1 to StubLevel.stubInput(model = PRO)),
                isModelsLoadFailed = true,
                models = StubModels.stubModels(),
            )

        val uiState = LevelsUiStateMapper(state) as UiState.Content

        assertEquals(
            expected = Triple(List(LevelCode.entries.size) { DEFAULT_MODEL }, false, MODELS_LOAD_ERROR),
            actual = Triple(uiState.rows.map { row -> row.model }, uiState.isModelChoiceEnabled, uiState.modelError),
        )
    }

    @Test
    fun `GIVEN a level model is no longer available WHEN mapping THEN its status shows with Refresh`() {
        val state = loaded(StubLevel.stubLevels())
            .copy(saveResult = SaveLevelsResult.ModelUnavailable)

        val uiState = LevelsUiStateMapper(state) as UiState.Content

        assertEquals(
            expected = Status(text = MODEL_UNAVAILABLE, tone = Status.Tone.ERROR) to true,
            actual = uiState.saveStatus to uiState.isRefreshShown,
        )
    }

    @Test
    fun `GIVEN a save in progress with the models WHEN mapping THEN the Model choice is disabled`() {
        val state = loaded(StubLevel.stubLevels())
            .copy(
                isSaving = true,
                models = StubModels.stubModels(),
            )

        val uiState = LevelsUiStateMapper(state) as UiState.Content

        assertEquals(expected = false, actual = uiState.isModelChoiceEnabled)
    }

    @Test
    fun `GIVEN the levels reload WHEN mapping THEN the reload status shows and the form is disabled`() {
        val state = loaded(StubLevel.stubLevels())
            .copy(
                isLoading = true,
                models = StubModels.stubModels(),
            )

        val uiState = LevelsUiStateMapper(state) as UiState.Content

        assertEquals(
            expected = Triple(Status(text = "Обновление…", tone = Status.Tone.MUTED), false, false),
            actual = Triple(uiState.saveStatus, uiState.isSaveEnabled, uiState.isModelChoiceEnabled),
        )
    }

    @Test
    fun `GIVEN the models load again WHEN mapping THEN Model is disabled with the loading sign`() {
        val state = loaded(StubLevel.stubLevels())
            .copy(
                isModelsLoading = true,
                models = StubModels.stubModels(),
            )

        val uiState = LevelsUiStateMapper(state) as UiState.Content

        assertEquals(
            expected = Triple("Загрузка моделей…", false, null),
            actual = Triple(uiState.modelProgress, uiState.isModelChoiceEnabled, uiState.modelError),
        )
    }

    private fun loaded(levels: List<Level>): DataState = DataState(
        forms = levels.toInputs(),
        levels = levels,
    )

    private companion object {

        const val MODEL_UNAVAILABLE =
            "Список моделей на сервере изменился: модель уровня недоступна. «Обновить» заменит правки данными сервера"

        const val MODELS_LOAD_ERROR = "Не удалось загрузить модели. Сохранение не меняет модели уровней"

        const val PRO = "deepseek-v4-pro"

        val DEFAULT_MODEL = UiState.ModelOption(
            label = "по умолчанию",
            model = null,
        )
    }
}
