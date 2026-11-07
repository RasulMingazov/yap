package app.yap.server.feature.topics.persistence

import app.yap.server.core.database.Database
import app.yap.server.core.database.withTestDatabase
import app.yap.server.feature.characters.model.CharacterProfile
import app.yap.server.feature.characters.persistence.DatabaseCharacterRepository
import app.yap.server.feature.topics.model.Interlocutor
import app.yap.server.feature.topics.model.Step
import app.yap.server.feature.topics.model.StubTopic
import app.yap.server.feature.topics.model.TopicContent
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

internal class DatabaseTopicRepositoryTest {

    @Test
    fun `GIVEN a clean database WHEN listing the topics THEN there are none`() = withTestDatabase { database ->
        runTest {
            val repository = DatabaseTopicRepository(database)

            val topics = repository.list()

            assertEquals(expected = emptyList(), actual = topics)
        }
    }

    @Test
    fun `GIVEN a created topic WHEN reading it THEN it has the same content`() = withTestDatabase { database ->
        runTest {
            val repository = DatabaseTopicRepository(database)
            val content = stubContent(interlocutor = database.createCast())
            val topic = repository.create(content)

            val found = repository.find(topic.topicId)

            assertEquals(expected = topic, actual = found)
        }
    }

    @Test
    fun `GIVEN an unknown topic WHEN reading it THEN there is no topic`() = withTestDatabase { database ->
        runTest {
            val repository = DatabaseTopicRepository(database)

            val found = repository.find(StubTopic.TOPIC_ID)

            assertNull(found)
        }
    }

    @Test
    fun `GIVEN a topic with steps WHEN reading it THEN the steps keep their order`() = withTestDatabase { database ->
        runTest {
            val repository = DatabaseTopicRepository(database)
            val steps = listOf("c", "a", "d", "b").map { task -> StubTopic.stubStep(task = task) }
            val topic = repository.create(stubContent(steps = steps))

            val found = repository.find(topic.topicId)

            assertEquals(expected = steps, actual = found?.content?.steps)
        }
    }

    @Test
    fun `GIVEN a topic with three steps WHEN updating it with one step THEN reading it returns one step`() =
        withTestDatabase { database ->
            runTest {
                val repository = DatabaseTopicRepository(database)
                val steps = listOf("a", "b", "c").map { task -> StubTopic.stubStep(task = task) }
                val topic = repository.create(stubContent(steps = steps))
                val changed = topic.copy(
                    content = topic.content.copy(
                        steps = listOf(StubTopic.stubStep(task = "z")),
                        title = "Changed",
                    ),
                )

                val updated = repository.update(changed)

                assertEquals(expected = true to changed, actual = updated to repository.find(topic.topicId))
            }
        }

    @Test
    fun `GIVEN an unknown topic WHEN updating it THEN nothing is stored`() = withTestDatabase { database ->
        runTest {
            val repository = DatabaseTopicRepository(database)

            val updated = repository.update(StubTopic.stubTopic())

            assertEquals(expected = false to emptyList(), actual = updated to repository.list())
        }
    }

    @Test
    fun `GIVEN topics WHEN listing them THEN they come by date and then by title without case`() =
        withTestDatabase { database ->
            runTest {
                val repository = DatabaseTopicRepository(database)
                val earlier = StubTopic.RELEASE_DATE.minusDays(1)
                repository.create(stubContent(title = "Sam"))
                repository.create(stubContent(title = "dan"))
                repository.create(stubContent(releaseDate = earlier, title = "Zed"))
                repository.create(stubContent(title = "Mia"))

                val titles = repository.list().map { topic -> topic.content.title }

                assertEquals(expected = listOf("Zed", "dan", "Mia", "Sam"), actual = titles)
            }
        }

    @Test
    fun `GIVEN topics with steps WHEN listing them THEN each topic has its own steps in order`() =
        withTestDatabase { database ->
            runTest {
                val repository = DatabaseTopicRepository(database)
                val first = repository.create(
                    stubContent(
                        steps = listOf("b", "a").map { task -> StubTopic.stubStep(task = task) },
                        title = "A",
                    ),
                )
                val second = repository.create(
                    stubContent(
                        steps = listOf("d", "c").map { task -> StubTopic.stubStep(task = task) },
                        title = "B",
                    ),
                )

                val topics = repository.list()

                assertEquals(expected = listOf(first, second), actual = topics)
            }
        }

    @Test
    fun `GIVEN a guest with an empty description WHEN reading the topic THEN it is still a guest`() =
        withTestDatabase { database ->
            runTest {
                val repository = DatabaseTopicRepository(database)
                val topic = repository.create(stubContent(interlocutor = Interlocutor.Guest(description = "")))

                val found = repository.find(topic.topicId)

                assertEquals(expected = Interlocutor.Guest(description = ""), actual = found?.content?.interlocutor)
            }
        }

    @Test
    fun `GIVEN a topic with a character WHEN changing it to a guest THEN the character is gone`() =
        withTestDatabase { database ->
            runTest {
                val repository = DatabaseTopicRepository(database)
                val topic = repository.create(stubContent(interlocutor = database.createCast()))
                val guest = Interlocutor.Guest(description = "A tourist.")

                repository.update(topic.copy(content = topic.content.copy(interlocutor = guest)))

                assertEquals(expected = guest, actual = repository.find(topic.topicId)?.content?.interlocutor)
            }
        }

    @Test
    fun `GIVEN a topic with no interlocutor and no duration WHEN reading it THEN both stay empty`() =
        withTestDatabase { database ->
            runTest {
                val repository = DatabaseTopicRepository(database)
                val topic = repository.create(stubContent(durationMinutes = null, interlocutor = null))

                val found = repository.find(topic.topicId)

                assertEquals(expected = topic, actual = found)
            }
        }
}

private suspend fun Database.createCast(): Interlocutor.Cast {
    val character = DatabaseCharacterRepository(this).create(
        CharacterProfile(
            history = "Drives a taxi.",
            manner = "Casual.",
            name = "Sam",
            personality = "Friendly.",
        ),
    )
    return Interlocutor.Cast(characterId = character.characterId)
}

private fun stubContent(
    durationMinutes: Int? = StubTopic.DURATION_MINUTES,
    interlocutor: Interlocutor? = null,
    releaseDate: LocalDate = StubTopic.RELEASE_DATE,
    steps: List<Step> = listOf(StubTopic.stubStep()),
    title: String = StubTopic.TITLE,
): TopicContent = StubTopic.stubContent(
    durationMinutes = durationMinutes,
    interlocutor = interlocutor,
    releaseDate = releaseDate,
    steps = steps,
    title = title,
)
