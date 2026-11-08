package app.yap.server.feature.topics.persistence

import app.yap.server.core.database.Database
import app.yap.server.core.database.withTestDatabase
import app.yap.server.feature.characters.model.StubCharacter
import app.yap.server.feature.characters.persistence.DatabaseCharacterRepository
import app.yap.server.feature.topics.model.Interlocutor
import app.yap.server.feature.topics.model.StubTopic
import app.yap.server.feature.topics.model.TopicContent
import app.yap.server.feature.topics.model.TopicId
import app.yap.server.feature.topics.model.TopicStatus
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.update

internal class DatabaseCharacterTopicCountsTest {

    @Test
    fun `GIVEN drafts, published and guest topics WHEN counting THEN only the topics of a character count`() =
        withTestDatabase { database ->
            runTest {
                val sam = database.createCast(name = "Sam")
                database.createCast(name = "Mia")
                val topics = DatabaseTopicRepository(database)
                topics.create(stubContent(interlocutor = sam, releaseDate = StubTopic.RELEASE_DATE))
                val first = topics.create(stubContent(interlocutor = sam, releaseDate = StubTopic.RELEASE_DATE))
                val second = topics.create(
                    stubContent(interlocutor = sam, releaseDate = StubTopic.RELEASE_DATE.plusDays(1)),
                )
                topics.create(stubContent(interlocutor = Interlocutor.Guest(description = "A tourist")))
                database.publish(first.topicId)
                database.publish(second.topicId)

                val counts = DatabaseCharacterTopicCounts(database).byCharacter()

                assertEquals(expected = mapOf(sam.characterId to 3), actual = counts)
            }
        }
}

private suspend fun Database.createCast(name: String): Interlocutor.Cast {
    val character = DatabaseCharacterRepository(this).create(StubCharacter.stubProfile(name = name))
    return Interlocutor.Cast(characterId = character.characterId)
}

private suspend fun Database.publish(topicId: TopicId) {
    transaction {
        TopicsTable.update(where = { TopicsTable.topicId eq topicId.value }) { statement ->
            statement[TopicsTable.status] = TopicStatus.PUBLISHED
        }
    }
}

private fun stubContent(
    interlocutor: Interlocutor,
    releaseDate: LocalDate = StubTopic.RELEASE_DATE,
): TopicContent = StubTopic.stubContent(
    interlocutor = interlocutor,
    releaseDate = releaseDate,
)
