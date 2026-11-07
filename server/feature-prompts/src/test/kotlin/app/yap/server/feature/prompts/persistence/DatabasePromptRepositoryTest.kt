package app.yap.server.feature.prompts.persistence

import app.yap.server.core.database.withTestDatabase
import app.yap.server.feature.prompts.model.Prompt
import app.yap.server.feature.prompts.model.PromptKey
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

internal class DatabasePromptRepositoryTest {

    @Test
    fun `GIVEN a clean database WHEN reading a prompt THEN it has no version`() = withTestDatabase { database ->
        runTest {
            val repository = DatabasePromptRepository(database)

            val prompt = repository.get(PromptKey.TOPIC_REPLY)

            assertEquals(
                expected = Prompt(current = null, promptKey = PromptKey.TOPIC_REPLY, versions = emptyList()),
                actual = prompt,
            )
        }
    }

    @Test
    fun `GIVEN two saves WHEN reading the prompt THEN the newest version is current`() = withTestDatabase { database ->
        runTest {
            val repository = DatabasePromptRepository(database)
            repository.save(PromptKey.TOPIC_REPLY, "First text.")
            repository.save(PromptKey.TOPIC_REPLY, "Second text.")

            val prompt = repository.get(PromptKey.TOPIC_REPLY)

            assertEquals(
                expected = 2 to listOf(2 to "Second text.", 1 to "First text."),
                actual = prompt.snapshot(),
            )
        }
    }

    @Test
    fun `GIVEN two saves WHEN reading the prompt THEN each version has its save time`() = withTestDatabase { database ->
        runTest {
            val repository = DatabasePromptRepository(database)
            val before = Instant.now().minusSeconds(CLOCK_SKEW_SECONDS)
            repository.save(PromptKey.TOPIC_REPLY, "First text.")
            repository.save(PromptKey.TOPIC_REPLY, "Second text.")
            val after = Instant.now().plusSeconds(CLOCK_SKEW_SECONDS)

            val prompt = repository.get(PromptKey.TOPIC_REPLY)

            val timeline = listOf(after) + prompt.versions.map { version -> version.savedAt } + before
            assertEquals(expected = timeline.sortedDescending(), actual = timeline)
        }
    }

    @Test
    fun `GIVEN two versions WHEN restoring the first THEN it is current, none is added`() = withTestDatabase { database ->
        runTest {
            val repository = DatabasePromptRepository(database)
            repository.save(PromptKey.TOPIC_REPLY, "First text.")
            repository.save(PromptKey.TOPIC_REPLY, "Second text.")

            val prompt = repository.restore(PromptKey.TOPIC_REPLY, number = 1)

            assertEquals(
                expected = 1 to listOf(2 to "Second text.", 1 to "First text."),
                actual = prompt?.snapshot(),
            )
        }
    }

    @Test
    fun `GIVEN a restored first version WHEN saving THEN the number follows the highest`() = withTestDatabase { database ->
        runTest {
            val repository = DatabasePromptRepository(database)
            repository.save(PromptKey.TOPIC_REPLY, "First text.")
            repository.save(PromptKey.TOPIC_REPLY, "Second text.")
            repository.restore(PromptKey.TOPIC_REPLY, number = 1)

            val prompt = repository.save(PromptKey.TOPIC_REPLY, "Third text.")

            assertEquals(
                expected = 3 to listOf(3 to "Third text.", 2 to "Second text.", 1 to "First text."),
                actual = prompt.snapshot(),
            )
        }
    }

    @Test
    fun `GIVEN two versions WHEN restoring an unknown version THEN nothing changes`() = withTestDatabase { database ->
        runTest {
            val repository = DatabasePromptRepository(database)
            repository.save(PromptKey.TOPIC_REPLY, "First text.")
            repository.save(PromptKey.TOPIC_REPLY, "Second text.")

            val restored = repository.restore(PromptKey.TOPIC_REPLY, number = 3)

            assertEquals(
                expected = null to (2 to listOf(2 to "Second text.", 1 to "First text.")),
                actual = restored to repository.get(PromptKey.TOPIC_REPLY).snapshot(),
            )
        }
    }
}

private fun Prompt.snapshot(): Pair<Int?, List<Pair<Int, String>>> =
    current?.number to versions.map { version -> version.number to version.text }

private const val CLOCK_SKEW_SECONDS = 5L
