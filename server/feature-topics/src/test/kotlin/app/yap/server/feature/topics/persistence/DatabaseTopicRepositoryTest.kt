package app.yap.server.feature.topics.persistence

import app.yap.server.core.database.Database
import app.yap.server.core.database.withRestartableDatabase
import app.yap.server.core.database.withTestDatabase
import app.yap.server.feature.characters.model.CharacterProfile
import app.yap.server.feature.characters.persistence.DatabaseCharacterRepository
import app.yap.server.feature.topics.model.Interlocutor
import app.yap.server.feature.topics.model.Step
import app.yap.server.feature.topics.model.StubTopic
import app.yap.server.feature.topics.model.TopicContent
import app.yap.server.feature.topics.model.TopicId
import app.yap.server.feature.topics.model.TopicStatus
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.update

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

                val outcome = repository.update(changed)

                assertEquals(
                    expected = TopicWrite.Written(changed) to changed,
                    actual = outcome to repository.find(topic.topicId),
                )
            }
        }

    @Test
    fun `GIVEN an unknown topic WHEN updating it THEN nothing is stored`() = withTestDatabase { database ->
        runTest {
            val repository = DatabaseTopicRepository(database)

            val outcome = repository.update(StubTopic.stubTopic())

            assertEquals(expected = TopicWrite.NotFound to emptyList(), actual = outcome to repository.list())
        }
    }

    @Test
    fun `GIVEN a created draft WHEN the database restarts THEN the topic is still a draft`() =
        withRestartableDatabase { open ->
            runTest {
                val topic = open()
                    .use { database ->
                        val repository = DatabaseTopicRepository(database)
                        repository.create(stubContent())
                    }

                val found = open()
                    .use { database ->
                        val repository = DatabaseTopicRepository(database)
                        repository.find(topic.topicId)
                    }

                assertEquals(expected = TopicStatus.DRAFT to topic, actual = found?.status to found)
            }
        }

    @Test
    fun `GIVEN a draft WHEN another draft takes the same date THEN both are stored`() = withTestDatabase { database ->
        runTest {
            val repository = DatabaseTopicRepository(database)
            val first = repository.create(stubContent(title = "First"))
            val second = repository.create(stubContent(title = "Second"))

            val topics = repository.list()

            assertEquals(expected = listOf(first, second), actual = topics)
        }
    }

    @Test
    fun `GIVEN a published topic WHEN updating its content THEN it stays published`() = withTestDatabase { database ->
        runTest {
            val repository = DatabaseTopicRepository(database)
            val topic = repository.create(stubContent())
            database.publish(topic.topicId)
            val changed = topic.copy(
                content = topic.content.copy(title = "Changed"),
                status = TopicStatus.PUBLISHED,
            )

            val outcome = repository.update(changed)

            assertEquals(
                expected = TopicWrite.Written(changed) to changed,
                actual = outcome to repository.find(topic.topicId),
            )
        }
    }

    @Test
    fun `GIVEN a topic published after it was read WHEN updating it as a draft THEN nothing is written`() =
        withTestDatabase { database ->
            runTest {
                val repository = DatabaseTopicRepository(database)
                val topic = repository.create(stubContent(steps = stubSteps("a", "b")))
                database.publish(topic.topicId)
                val changed = topic.copy(content = topic.content.copy(steps = stubSteps("z"), title = "Changed"))

                val outcome = repository.update(changed)

                assertEquals(
                    expected = TopicWrite.StaleStatus to topic.copy(status = TopicStatus.PUBLISHED),
                    actual = outcome to repository.find(topic.topicId),
                )
            }
        }

    @Test
    fun `GIVEN a date with a published topic WHEN moving another published topic onto it THEN nothing is written`() =
        withTestDatabase { database ->
            runTest {
                val repository = DatabaseTopicRepository(database)
                val taken = repository.create(stubContent(title = "Taken"))
                val other = repository.create(
                    stubContent(releaseDate = StubTopic.RELEASE_DATE.plusDays(1), steps = stubSteps("a", "b")),
                )
                database.publish(taken.topicId)
                database.publish(other.topicId)
                val moved = other.copy(
                    content = other.content.copy(releaseDate = StubTopic.RELEASE_DATE, steps = stubSteps("z")),
                    status = TopicStatus.PUBLISHED,
                )

                val outcome = repository.update(moved)

                assertEquals(
                    expected = TopicWrite.DateTaken to other.copy(status = TopicStatus.PUBLISHED),
                    actual = outcome to repository.find(other.topicId),
                )
            }
        }

    @Test
    fun `GIVEN a draft WHEN publishing it and restarting THEN the topic is published with the new content`() =
        withRestartableDatabase { open ->
            runTest {
                val draft = open()
                    .use { database ->
                        val repository = DatabaseTopicRepository(database)
                        repository.create(stubContent(steps = stubSteps("a", "b")))
                    }
                val content = draft.content.copy(steps = stubSteps("z"), title = "Changed")
                val published = draft.copy(content = content, status = TopicStatus.PUBLISHED)

                val outcome = open()
                    .use { database ->
                        val repository = DatabaseTopicRepository(database)
                        repository.publish(content, draft.topicId)
                    }

                val found = open()
                    .use { database ->
                        val repository = DatabaseTopicRepository(database)
                        repository.find(draft.topicId)
                    }
                assertEquals(expected = TopicPublish.Published(published) to published, actual = outcome to found)
            }
        }

    @Test
    fun `GIVEN an unknown topic WHEN publishing it THEN nothing is stored`() = withTestDatabase { database ->
        runTest {
            val repository = DatabaseTopicRepository(database)

            val outcome = repository.publish(stubContent(), StubTopic.TOPIC_ID)

            assertEquals(expected = TopicPublish.NotFound to emptyList(), actual = outcome to repository.list())
        }
    }

    @Test
    fun `GIVEN a date with a published topic WHEN publishing a draft of that date THEN the draft stays as stored`() =
        withTestDatabase { database ->
            runTest {
                val repository = DatabaseTopicRepository(database)
                val taken = repository.create(stubContent(title = "Taken"))
                val draft = repository.create(stubContent(steps = stubSteps("a", "b")))
                repository.publish(taken.content, taken.topicId)

                val outcome = repository.publish(draft.content.copy(steps = stubSteps("z")), draft.topicId)

                assertEquals(
                    expected = TopicPublish.DateTaken to draft,
                    actual = outcome to repository.find(draft.topicId),
                )
            }
        }

    @Test
    fun `GIVEN published topics of two dates WHEN publishing one with the date of the other THEN nothing is written`() =
        withTestDatabase { database ->
            runTest {
                val repository = DatabaseTopicRepository(database)
                val taken = repository.create(stubContent(title = "Taken"))
                val later = repository.create(stubContent(releaseDate = StubTopic.RELEASE_DATE.plusDays(1)))
                repository.publish(taken.content, taken.topicId)
                repository.publish(later.content, later.topicId)

                val moved = later.content.copy(releaseDate = StubTopic.RELEASE_DATE)

                val outcome = repository.publish(moved, later.topicId)

                assertEquals(
                    expected = TopicPublish.DateTaken to later.copy(status = TopicStatus.PUBLISHED),
                    actual = outcome to repository.find(later.topicId),
                )
            }
        }

    @Test
    fun `GIVEN two drafts of one date WHEN publishing both at once THEN one is published and the other is unchanged`() =
        withTestDatabase { database ->
            runTest {
                val repository = DatabaseTopicRepository(database)
                val first = repository.create(stubContent(title = "A"))
                val second = repository.create(stubContent(title = "B"))
                val drafts = listOf(first, second)
                val start = CompletableDeferred<Unit>()
                val outcomes = drafts.map { draft ->
                    async(Dispatchers.Default) {
                        start.await()
                        repository.publish(draft.content.copy(steps = stubSteps("z")), draft.topicId)
                    }
                }

                start.complete(Unit)
                val written = outcomes
                    .awaitAll()
                    .filterIsInstance<TopicPublish.Published>()
                    .single()
                    .topic

                val stored = repository.list().associateBy { topic -> topic.topicId }
                val other = drafts.single { draft -> draft.topicId != written.topicId }
                assertEquals(
                    expected = listOf(written, other),
                    actual = listOf(stored.getValue(written.topicId), stored.getValue(other.topicId)),
                )
            }
        }

    @Test
    fun `GIVEN another topic published on a date WHEN asking about the date THEN a published topic is there`() =
        withTestDatabase { database ->
            runTest {
                val repository = DatabaseTopicRepository(database)
                val published = repository.create(stubContent())
                val asking = repository.create(stubContent())
                repository.publish(published.content, published.topicId)

                val taken = repository.hasPublishedOn(StubTopic.RELEASE_DATE, asking.topicId)

                assertTrue(taken)
            }
        }

    @Test
    fun `GIVEN only the topic itself published on a date WHEN asking about the date THEN no other is there`() =
        withTestDatabase { database ->
            runTest {
                val repository = DatabaseTopicRepository(database)
                val published = repository.create(stubContent())
                repository.publish(published.content, published.topicId)

                val taken = repository.hasPublishedOn(StubTopic.RELEASE_DATE, published.topicId)

                assertFalse(taken)
            }
        }

    @Test
    fun `GIVEN only drafts on a date WHEN asking about the date THEN no published topic of that date is there`() =
        withTestDatabase { database ->
            runTest {
                val repository = DatabaseTopicRepository(database)
                repository.create(stubContent())
                val asking = repository.create(stubContent())
                val other = repository.create(stubContent(releaseDate = StubTopic.RELEASE_DATE.plusDays(1)))
                repository.publish(other.content, other.topicId)

                val taken = repository.hasPublishedOn(StubTopic.RELEASE_DATE, asking.topicId)

                assertFalse(taken)
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

private suspend fun Database.publish(topicId: TopicId) {
    transaction {
        TopicsTable.update(where = { TopicsTable.topicId eq topicId.value }) { statement ->
            statement[TopicsTable.status] = TopicStatus.PUBLISHED
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
