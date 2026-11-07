package app.yap.server.feature.levels.persistence

import app.yap.server.feature.levels.model.LevelEntry
import app.yap.server.feature.levels.model.StubLevel
import io.github.rasulmingazov.stubcall.StubCall0
import io.github.rasulmingazov.stubcall.StubCall1

class StubLevelRepository(
    table: List<LevelEntry> = StubLevel.stubTable(),
) : LevelRepository {

    val listCall = StubCall0.returns<List<LevelEntry>>(table)
    val saveCall = StubCall1.unit<List<LevelEntry>>()

    override suspend fun list(): List<LevelEntry> = listCall.invoke()

    override suspend fun save(entries: List<LevelEntry>) = saveCall.invoke(entries)
}
