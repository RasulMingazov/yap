package app.yap.server.feature.levels.usecase

import app.yap.server.feature.levels.model.Level
import app.yap.server.feature.levels.model.LevelCode
import app.yap.server.feature.levels.model.StubLevel
import app.yap.server.feature.levels.persistence.StubLevelRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

internal class GetLevelsUseCaseTest {

    @Test
    fun `GIVEN no saved levels WHEN reading THEN all seven come in order with no values`() = runTest {
        val useCase = GetLevelsUseCase(levelRepository = StubLevelRepository(table = emptyList()))

        val levels = useCase()

        assertEquals(
            expected = LevelCode.entries.map { code -> Level(code = code, range = null, settings = null) },
            actual = levels,
        )
    }

    @Test
    fun `GIVEN a saved table WHEN reading THEN B1 comes with its values and its range`() = runTest {
        val useCase = GetLevelsUseCase(levelRepository = StubLevelRepository(table = StubLevel.stubTable()))

        val levels = useCase()

        assertEquals(
            expected = StubLevel.stubLevel(),
            actual = levels.single { level -> level.code == LevelCode.B1 },
        )
    }
}
