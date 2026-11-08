package app.yap.admin.feature.levels.domain.usecase

import app.yap.admin.feature.levels.domain.LevelCode
import app.yap.admin.feature.levels.domain.SaveLevelsResult
import app.yap.admin.feature.levels.domain.SaveLevelsResult.NumberField
import app.yap.admin.feature.levels.domain.StubLevel
import app.yap.admin.feature.levels.domain.repository.StubLevelRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

internal class DefaultSaveLevelsUseCaseTest {

    @Test
    fun `GIVEN A1 and No rating without a rating from WHEN saving THEN both are sent`() = runTest {
        val env = Environment()
        val noRatingFrom = StubLevel.stubInput(ratingFrom = "")

        env.useCase(mapOf(LevelCode.A1 to noRatingFrom, LevelCode.NO_RATING to noRatingFrom))

        env.levelRepository.saveCall.calledWith(
            listOf(
                StubLevel.stubEntry(LevelCode.A1, StubLevel.stubSettings(ratingFrom = null)),
                StubLevel.stubEntry(LevelCode.NO_RATING, StubLevel.stubSettings(ratingFrom = null)),
            ),
        )
    }

    @Test
    fun `GIVEN B1 with an empty block WHEN saving THEN it is sent and the answer comes back`() = runTest {
        val env = Environment()
        env.levelRepository.saveCall.returns(SaveLevelsResult.Invalid)

        val result = env.useCase(mapOf(LevelCode.B1 to StubLevel.stubInput(block = "")))

        env.levelRepository.saveCall.calledWith(listOf(StubLevel.stubEntry(settings = StubLevel.stubSettings(block = ""))))
        assertEquals(expected = SaveLevelsResult.Invalid, actual = result)
    }

    @Test
    fun `GIVEN an unread rating from WHEN saving THEN it names the level and sends nothing`() = runTest {
        val env = Environment()

        val result = env.useCase(mapOf(LevelCode.A2 to StubLevel.stubInput(ratingFrom = "10a")))

        env.levelRepository.saveCall.notCalled()
        assertEquals(expected = SaveLevelsResult.UnreadNumber(LevelCode.A2, NumberField.RATING_FROM), actual = result)
    }

    @Test
    fun `GIVEN an unread word count WHEN saving THEN it names the level and sends nothing`() = runTest {
        val env = Environment()

        val result = env.useCase(mapOf(LevelCode.NO_RATING to StubLevel.stubInput(maxWords = "16a")))

        env.levelRepository.saveCall.notCalled()
        assertEquals(expected = SaveLevelsResult.UnreadNumber(LevelCode.NO_RATING, NumberField.MAX_WORDS), actual = result)
    }

    @Test
    fun `GIVEN an empty word count WHEN saving THEN it asks for the word count and sends nothing`() = runTest {
        val env = Environment()

        val result = env.useCase(mapOf(LevelCode.B1 to StubLevel.stubInput(maxWords = " ")))

        env.levelRepository.saveCall.notCalled()
        assertEquals(expected = SaveLevelsResult.NoWordCount(LevelCode.B1), actual = result)
    }

    @Test
    fun `GIVEN numbers with spaces around them WHEN saving THEN the numbers are sent`() = runTest {
        val env = Environment()

        env.useCase(mapOf(LevelCode.B1 to StubLevel.stubInput(maxWords = " 16", ratingFrom = "1050 ")))

        env.levelRepository.saveCall.calledWith(listOf(StubLevel.stubEntry()))
    }

    @Test
    fun `GIVEN a level without idioms WHEN saving THEN it names the level and sends nothing`() = runTest {
        val env = Environment()

        val result = env.useCase(mapOf(LevelCode.C1 to StubLevel.stubInput(idioms = null)))

        env.levelRepository.saveCall.notCalled()
        assertEquals(expected = SaveLevelsResult.NoIdioms(LevelCode.C1), actual = result)
    }

    private class Environment {

        val levelRepository = StubLevelRepository()
        val useCase = DefaultSaveLevelsUseCase(levelRepository = levelRepository)
    }
}
