package app.yap.admin.feature.levels.domain.usecase

import app.yap.admin.feature.levels.domain.LevelEntry
import app.yap.admin.feature.levels.domain.SaveLevelsResult
import app.yap.admin.feature.levels.domain.StubLevel
import io.github.rasulmingazov.stubcall.StubCall1

internal class StubSaveLevelsUseCase(
    result: SaveLevelsResult = SaveLevelsResult.Saved(StubLevel.stubLevels()),
) : SaveLevelsUseCase {

    val invokeCall = StubCall1.returns<List<LevelEntry>, SaveLevelsResult>(result)

    override suspend fun invoke(entries: List<LevelEntry>): SaveLevelsResult = invokeCall.invoke(entries)
}
