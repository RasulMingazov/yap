package app.yap.server.feature.levels.usecase

import app.yap.server.feature.levels.model.LevelCode
import app.yap.server.feature.levels.model.StubLevel
import app.yap.server.feature.levels.persistence.StubLevelRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

internal class GetLevelRulesUseCaseTest {

    @Test
    fun `GIVEN a saved block for B1 WHEN asking for the rules of B1 THEN the value is that block`() = runTest {
        val env = Environment()
        env.levelRepository.listCall.returns(
            StubLevel.stubTable(b1 = StubLevel.stubSettings(block = "Speak naturally.")),
        )

        val rules = env.useCase(LevelCode.B1)

        assertEquals(expected = "Speak naturally.", actual = rules)
    }

    @Test
    fun `GIVEN levels that were not saved WHEN asking for the rules of B1 THEN there is no value`() = runTest {
        val env = Environment()
        env.levelRepository.listCall.returns(emptyList())

        val rules = env.useCase(LevelCode.B1)

        assertNull(rules)
    }

    private class Environment {

        val levelRepository = StubLevelRepository()
        val useCase = GetLevelRulesUseCase(levelRepository = levelRepository)
    }
}
