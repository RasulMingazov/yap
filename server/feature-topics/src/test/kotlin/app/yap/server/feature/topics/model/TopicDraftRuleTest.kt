package app.yap.server.feature.topics.model

import app.yap.server.feature.characters.persistence.StubCharacterRepository
import app.yap.server.feature.characters.usecase.GetCharacterUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

internal class TopicDraftRuleTest {

    @Test
    fun `GIVEN a draft that is ready and a character that exists WHEN checking THEN there are no gaps`() =
        runTest {
            val env = Environment()

            val gaps = env.rule.gaps(StubTopic.stubContent())

            assertEquals(expected = emptySet(), actual = gaps)
        }

    @Test
    fun `GIVEN only a title and a date WHEN checking THEN there are no gaps`() = runTest {
        val env = Environment()
        val content = StubTopic.stubContent(
            description = "",
            durationMinutes = null,
            interlocutor = null,
            openingLine = StubTopic.stubOpeningLine(english = "", translation = ""),
            role = "",
            setting = "",
            steps = emptyList(),
        )

        val gaps = env.rule.gaps(content)

        assertEquals(expected = emptySet(), actual = gaps)
    }

    @Test
    fun `GIVEN an empty title WHEN checking THEN the title is missing`() = runTest {
        val env = Environment()

        val gaps = env.rule.gaps(StubTopic.stubContent(title = ""))

        assertEquals(expected = setOf(TopicGap.Missing(TopicField.TITLE)), actual = gaps)
    }

    @Test
    fun `GIVEN no release date WHEN checking THEN the release date is missing`() = runTest {
        val env = Environment()

        val gaps = env.rule.gaps(StubTopic.stubContent(releaseDate = null))

        assertEquals(expected = setOf(TopicGap.Missing(TopicField.RELEASE_DATE)), actual = gaps)
    }

    @Test
    fun `GIVEN a duration of 0 minutes WHEN checking THEN the duration is missing`() = runTest {
        val env = Environment()

        val gaps = env.rule.gaps(StubTopic.stubContent(durationMinutes = 0))

        assertEquals(expected = setOf(TopicGap.Missing(TopicField.DURATION)), actual = gaps)
    }

    @Test
    fun `GIVEN a duration of 1 minute WHEN checking THEN there are no gaps`() = runTest {
        val env = Environment()

        val gaps = env.rule.gaps(StubTopic.stubContent(durationMinutes = 1))

        assertEquals(expected = emptySet(), actual = gaps)
    }

    @Test
    fun `GIVEN a character that does not exist WHEN checking THEN the interlocutor is missing`() = runTest {
        val env = Environment()
        env.characterRepository.findCall.returns(null)

        val gaps = env.rule.gaps(StubTopic.stubContent())

        assertEquals(expected = setOf(TopicGap.Missing(TopicField.INTERLOCUTOR)), actual = gaps)
    }

    @Test
    fun `GIVEN a guest and no characters WHEN checking THEN there are no gaps`() = runTest {
        val env = Environment()
        env.characterRepository.findCall.returns(null)

        val gaps = env.rule.gaps(
            StubTopic.stubContent(interlocutor = Interlocutor.Guest(description = "A tourist.")),
        )

        assertEquals(expected = emptySet(), actual = gaps)
    }

    @Test
    fun `GIVEN a guest with an empty description WHEN checking THEN there are no gaps`() = runTest {
        val env = Environment()

        val gaps = env.rule.gaps(StubTopic.stubContent(interlocutor = Interlocutor.Guest(description = "")))

        assertEquals(expected = emptySet(), actual = gaps)
    }

    private class Environment {

        val characterRepository = StubCharacterRepository()
        val rule = TopicDraftRule(
            getCharacterUseCase = GetCharacterUseCase(characterRepository = characterRepository),
        )
    }
}
