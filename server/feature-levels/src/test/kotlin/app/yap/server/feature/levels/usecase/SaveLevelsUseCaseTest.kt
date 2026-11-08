package app.yap.server.feature.levels.usecase

import app.yap.server.feature.levels.model.LevelCode
import app.yap.server.feature.levels.model.SaveLevelsOutcome
import app.yap.server.feature.levels.model.StubLevel
import app.yap.server.feature.levels.persistence.StubLevelRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest

internal class SaveLevelsUseCaseTest {

    @Test
    fun `GIVEN a filled table WHEN saving THEN the table is stored and the levels come back with ranges`() = runTest {
        val env = Environment()
        val table = StubLevel.stubTable()

        val outcome = env.useCase(table)

        env.levelRepository.saveCall.calledWith(table)
        val saved = assertIs<SaveLevelsOutcome.Saved>(outcome)
        assertEquals(
            expected = StubLevel.stubLevel(),
            actual = saved.levels.single { level -> level.code == LevelCode.B1 },
        )
    }

    @Test
    fun `GIVEN a block with spaces around WHEN saving THEN the trimmed table is stored`() = runTest {
        val env = Environment()
        val table = StubLevel.stubTable(b1 = StubLevel.stubSettings(block = " ${StubLevel.BLOCK} "))

        env.useCase(table)

        env.levelRepository.saveCall.calledWith(StubLevel.stubTable())
    }

    @Test
    fun `GIVEN a table without C2 WHEN saving THEN nothing is stored`() = runTest {
        val env = Environment()
        val table = StubLevel.stubTable().filter { entry -> entry.code != LevelCode.C2 }

        val outcome = env.useCase(table)

        env.levelRepository.saveCall.notCalled()
        assertEquals(expected = SaveLevelsOutcome.Invalid, actual = outcome)
    }

    @Test
    fun `GIVEN a level with a model outside the catalog WHEN saving THEN nothing is stored`() = runTest {
        val env = Environment()
        val table = StubLevel.stubTable(b1 = StubLevel.stubSettings(model = "deepseek-chat"))

        val outcome = env.useCase(table)

        env.levelRepository.saveCall.notCalled()
        assertEquals(expected = SaveLevelsOutcome.ModelUnavailable, actual = outcome)
    }

    private class Environment {

        val levelRepository = StubLevelRepository()
        val useCase = SaveLevelsUseCase(levelRepository = levelRepository)
    }
}
