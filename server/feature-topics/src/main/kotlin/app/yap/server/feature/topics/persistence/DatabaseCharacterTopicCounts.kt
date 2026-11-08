package app.yap.server.feature.topics.persistence

import app.yap.server.core.database.Database
import app.yap.server.feature.characters.model.CharacterId
import app.yap.server.feature.characters.usecase.CharacterTopicCounts
import org.jetbrains.exposed.v1.core.count
import org.jetbrains.exposed.v1.core.isNotNull
import org.jetbrains.exposed.v1.jdbc.select

internal class DatabaseCharacterTopicCounts(
    private val database: Database,
) : CharacterTopicCounts {

    override suspend fun byCharacter(): Map<CharacterId, Int> = database.transaction {
        val topicCount = TopicsTable.topicId.count()
        TopicsTable
            .select(TopicsTable.characterId, topicCount)
            .where { TopicsTable.characterId.isNotNull() }
            .groupBy(TopicsTable.characterId)
            .associate { row ->
                CharacterId(requireNotNull(row[TopicsTable.characterId])) to row[topicCount].toInt()
            }
    }
}
