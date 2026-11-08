package app.yap.admin.feature.levels.domain.usecase

import app.yap.admin.feature.levels.domain.LevelCode
import app.yap.admin.feature.levels.domain.LevelInput
import app.yap.admin.feature.levels.domain.SaveLevelsResult
import app.yap.admin.feature.levels.domain.StubLevel
import io.github.rasulmingazov.stubcall.StubCall1

internal class StubSaveLevelsUseCase(
    result: SaveLevelsResult = SaveLevelsResult.Saved(StubLevel.stubLevels()),
) : SaveLevelsUseCase {

    val invokeCall = StubCall1.returns<Map<LevelCode, LevelInput>, SaveLevelsResult>(result)

    override suspend fun invoke(inputs: Map<LevelCode, LevelInput>): SaveLevelsResult = invokeCall.invoke(inputs)
}
