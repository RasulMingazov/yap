package app.yap.server.feature.levels.persistence

import app.yap.server.core.database.Database
import app.yap.server.feature.levels.model.LevelEntry
import app.yap.server.feature.levels.model.LevelSettings
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert

class DatabaseLevelRepository(
    private val database: Database,
) : LevelRepository {

    override suspend fun list(): List<LevelEntry> = database.transaction {
        LevelsTable
            .selectAll()
            .map { row -> row.toEntry() }
    }

    override suspend fun save(entries: List<LevelEntry>) {
        database.transaction {
            entries.forEach { entry ->
                LevelsTable.upsert { statement -> statement.write(entry) }
            }
        }
    }
}

private fun ResultRow.toEntry(): LevelEntry = LevelEntry(
    code = this[LevelsTable.code],
    settings = LevelSettings(
        block = this[LevelsTable.block],
        idioms = this[LevelsTable.idioms],
        maxWords = this[LevelsTable.maxWords],
        ratingFrom = this[LevelsTable.ratingFrom],
        vocabulary = this[LevelsTable.vocabulary],
    ),
)

private fun UpdateBuilder<*>.write(entry: LevelEntry) {
    this[LevelsTable.block] = entry.settings.block
    this[LevelsTable.code] = entry.code
    this[LevelsTable.idioms] = entry.settings.idioms
    this[LevelsTable.maxWords] = entry.settings.maxWords
    this[LevelsTable.ratingFrom] = entry.settings.ratingFrom
    this[LevelsTable.vocabulary] = entry.settings.vocabulary
}
