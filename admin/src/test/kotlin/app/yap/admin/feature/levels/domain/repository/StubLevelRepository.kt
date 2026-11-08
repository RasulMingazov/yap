package app.yap.admin.feature.levels.domain.repository

import app.yap.admin.core.common.LoadResult
import app.yap.admin.feature.levels.domain.Level
import app.yap.admin.feature.levels.domain.LevelEntry
import app.yap.admin.feature.levels.domain.SaveLevelsResult
import app.yap.admin.feature.levels.domain.StubLevel
import io.github.rasulmingazov.stubcall.StubCall0
import io.github.rasulmingazov.stubcall.StubCall1

internal class StubLevelRepository(
    levels: List<Level> = StubLevel.stubLevels(),
) : LevelRepository {

    val listCall = StubCall0.returns<LoadResult<List<Level>>>(LoadResult.Loaded(levels))
    val saveCall = StubCall1.returns<List<LevelEntry>, SaveLevelsResult>(SaveLevelsResult.Saved(levels))

    override suspend fun list(): LoadResult<List<Level>> = listCall.invoke()

    override suspend fun save(entries: List<LevelEntry>): SaveLevelsResult = saveCall.invoke(entries)
}
