package app.yap.server.feature.models.usecase

import app.yap.server.core.llm.ModelCatalog
import app.yap.server.feature.levels.model.LevelCode
import app.yap.server.feature.levels.model.LevelEntry
import app.yap.server.feature.levels.model.StubLevel
import app.yap.server.feature.levels.persistence.StubLevelRepository
import app.yap.server.feature.levels.usecase.GetLevelsUseCase
import app.yap.server.feature.models.model.ModelTask
import app.yap.server.feature.models.model.StubTaskModel
import app.yap.server.feature.models.model.TaskModel
import app.yap.server.feature.models.persistence.StubTaskModelRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

internal class GetTaskModelUseCaseTest {

    @Test
    fun `GIVEN a level without a model WHEN asking for Character reply THEN the task model comes`() = runTest {
        val env = Environment(
            levels = StubLevel.stubTable(b1 = StubLevel.stubSettings(model = ModelCatalog.DEFAULT.model)),
            tasks = StubTaskModel.stubTable(reply = StubTaskModel.stubChoice()),
        )

        val choice = env.useCase(code = LevelCode.B2, task = ModelTask.TOPIC_REPLY)

        assertEquals(expected = StubTaskModel.stubChoice(), actual = choice)
    }

    @Test
    fun `GIVEN a level with a model WHEN asking for Character reply THEN the level model comes`() = runTest {
        val env = Environment(levels = levelWithModel())

        val choice = env.useCase(code = LevelCode.B1, task = ModelTask.TOPIC_REPLY)

        assertEquals(expected = StubTaskModel.stubChoice(), actual = choice)
    }

    @Test
    fun `GIVEN a level with a model WHEN asking for Hints THEN the level model comes`() = runTest {
        val env = Environment(levels = levelWithModel())

        val choice = env.useCase(code = LevelCode.B1, task = ModelTask.TOPIC_HINTS)

        assertEquals(expected = StubTaskModel.stubChoice(), actual = choice)
    }

    @Test
    fun `GIVEN a level with a model WHEN asking for Step check THEN the task model comes`() = runTest {
        val env = Environment(levels = levelWithModel())

        val choice = env.useCase(code = LevelCode.B1, task = ModelTask.TOPIC_CHECK)

        assertEquals(expected = ModelCatalog.DEFAULT, actual = choice)
    }

    @Test
    fun `GIVEN a level with a model WHEN asking for Review THEN the task model comes`() = runTest {
        val env = Environment(levels = levelWithModel())

        val choice = env.useCase(code = LevelCode.B1, task = ModelTask.TOPIC_REVIEW)

        assertEquals(expected = ModelCatalog.DEFAULT, actual = choice)
    }

    @Test
    fun `GIVEN a level with a model WHEN asking for Calibration THEN the task model comes`() = runTest {
        val env = Environment(levels = levelWithModel())

        val choice = env.useCase(code = LevelCode.B1, task = ModelTask.CALIBRATION)

        assertEquals(expected = ModelCatalog.DEFAULT, actual = choice)
    }

    @Test
    fun `GIVEN a task row outside the catalog WHEN asking for Review THEN the default comes`() = runTest {
        val env = Environment(tasks = StubTaskModel.stubTable(review = StubTaskModel.stubChoice(model = "deepseek-chat")))

        val choice = env.useCase(code = LevelCode.B1, task = ModelTask.TOPIC_REVIEW)

        assertEquals(expected = ModelCatalog.DEFAULT, actual = choice)
    }

    @Test
    fun `GIVEN a call for Review WHEN the owner saves another model THEN the next call uses it`() = runTest {
        val env = Environment()
        env.useCase(code = LevelCode.B1, task = ModelTask.TOPIC_REVIEW)
        env.taskModelRepository.listCall.returns(StubTaskModel.stubTable(review = StubTaskModel.stubChoice()))

        val choice = env.useCase(code = LevelCode.B1, task = ModelTask.TOPIC_REVIEW)

        assertEquals(expected = StubTaskModel.stubChoice(), actual = choice)
    }

    private fun levelWithModel(): List<LevelEntry> =
        StubLevel.stubTable(b1 = StubLevel.stubSettings(model = StubLevel.MODEL))

    private class Environment(
        levels: List<LevelEntry> = StubLevel.stubTable(),
        tasks: List<TaskModel> = StubTaskModel.stubTable(),
    ) {

        val taskModelRepository = StubTaskModelRepository(models = tasks)
        val useCase = GetTaskModelUseCase(
            getLevelsUseCase = GetLevelsUseCase(levelRepository = StubLevelRepository(table = levels)),
            taskModelRepository = taskModelRepository,
        )
    }
}
