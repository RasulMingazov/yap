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
    fun `GIVEN a created topic with steps WHEN reading it THEN it has the same content and step order`() =
        withTestDatabase { database ->
            runTest {
                val repository = DatabaseTopicRepository(database)
                val content = stubContent(interlocutor = database.createCast(), steps = stubSteps("c", "a", "d", "b"))
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
    fun `GIVEN a topic with three steps WHEN updating it with one step THEN reading it returns one step`() =
        withTestDatabase { database ->
            runTest {
                val repository = DatabaseTopicRepository(database)
                val topic = repository.create(stubContent(steps = stubSteps("a", "b", "c")))
                val changed = topic.copy(content = topic.content.copy(steps = stubSteps("z"), title = "Changed"))

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
    fun `GIVEN topics with steps WHEN listing them THEN they come by date and title, each with its own steps`() =
        withTestDatabase { database ->
            runTest {
                val repository = DatabaseTopicRepository(database)
                val earlier = StubTopic.RELEASE_DATE.minusDays(1)
                val sam = repository.create(stubContent(steps = stubSteps("b", "a"), title = "Sam"))
                val dan = repository.create(stubContent(steps = stubSteps("d", "c"), title = "dan"))
                val zed = repository.create(stubContent(releaseDate = earlier, title = "Zed"))
                val mia = repository.create(stubContent(steps = stubSteps("f", "e"), title = "Mia"))

                val topics = repository.list()

                assertEquals(expected = listOf(zed, dan, mia, sam), actual = topics)
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
    fun `GIVEN an empty draft with only a title and a date WHEN reading it THEN it has no steps and no extras`() =
        withTestDatabase { database ->
            runTest {
                val repository = DatabaseTopicRepository(database)
                val topic = repository.create(stubContent(durationMinutes = null, steps = emptyList()))

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

private fun stubSteps(vararg tasks: String): List<Step> = tasks.map { task -> StubTopic.stubStep(task = task) }

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
