package app.yap.server.feature.levels.persistence

import app.yap.server.feature.levels.model.LevelEntry

interface LevelRepository {

    suspend fun list(): List<LevelEntry>

    suspend fun save(entries: List<LevelEntry>)
}
