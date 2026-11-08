package app.yap.admin.feature.levels.domain.repository

import app.yap.admin.core.common.LoadResult
import app.yap.admin.feature.levels.domain.Level
import app.yap.admin.feature.levels.domain.LevelEntry
import app.yap.admin.feature.levels.domain.SaveLevelsResult

internal interface LevelRepository {

    suspend fun list(): LoadResult<List<Level>>

    suspend fun save(entries: List<LevelEntry>): SaveLevelsResult
}
