package app.yap.server.feature.topics.model

import app.yap.server.feature.characters.persistence.StubCharacterRepository
import app.yap.server.feature.characters.usecase.GetCharacterUseCase
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

internal class TopicPublishRuleTest {

    @Test
    fun `GIVEN a topic with every field WHEN publishing THEN there are no gaps`() = runTest {
        val env = Environment()

        val gaps = env.gaps(StubTopic.stubContent())

        assertEquals(expected = emptySet(), actual = gaps)
    }

    @Test
    fun `GIVEN a topic with only a title and a date WHEN publishing THEN every other field is missing`() =
        runTest {
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

            val gaps = env.gaps(content)

            assertEquals(
                expected = setOf(
                    TopicGap.Missing(TopicField.DURATION),
                    TopicGap.Missing(TopicField.DESCRIPTION),
                    TopicGap.Missing(TopicField.SETTING),
                    TopicGap.Missing(TopicField.INTERLOCUTOR),
                    TopicGap.Missing(TopicField.ROLE),
                    TopicGap.Missing(TopicField.OPENING_LINE),
                    TopicGap.Missing(TopicField.TRANSLATION),
                    TopicGap.Missing(TopicField.STEPS),
                ),
                actual = gaps,
            )
        }

    @Test
    fun `GIVEN no setting and the steps 3 and 5 without an outcome WHEN publishing THEN the gaps name them`() =
        runTest {
            val env = Environment()
            val content = StubTopic.stubContent(
                setting = "",
                steps = listOf(
                    StubTopic.stubStep(),
                    StubTopic.stubStep(),
                    StubTopic.stubStep(outcome = ""),
                    StubTopic.stubStep(),
                    StubTopic.stubStep(outcome = ""),
                ),
            )

            val gaps = env.gaps(content)

            assertEquals(
                expected = setOf(TopicGap.Missing(TopicField.SETTING), TopicGap.IncompleteSteps(listOf(3, 5))),
                actual = gaps,
            )
        }

    @Test
    fun `GIVEN a guest without a description WHEN publishing THEN the interlocutor is missing`() = runTest {
        val env = Environment()

        val gaps = env.gaps(StubTopic.stubContent(interlocutor = Interlocutor.Guest(description = "")))

        assertEquals(expected = setOf(TopicGap.Missing(TopicField.INTERLOCUTOR)), actual = gaps)
    }

    @Test
    fun `GIVEN a draft with a past date WHEN publishing THEN the date is past`() = runTest {
        val env = Environment()

        val gaps = env.gaps(StubTopic.stubContent(), today = StubTopic.RELEASE_DATE.plusDays(1))

        assertEquals(expected = setOf(TopicGap.PastDate), actual = gaps)
    }

    @Test
    fun `GIVEN a published topic that keeps its past date WHEN saving THEN there are no gaps`() = runTest {
        val env = Environment()

        val gaps = env.gaps(
            StubTopic.stubContent(),
            publishedDate = StubTopic.RELEASE_DATE,
            today = StubTopic.RELEASE_DATE.plusDays(1),
        )

        assertEquals(expected = emptySet(), actual = gaps)
    }

    @Test
    fun `GIVEN a published topic moved into the past WHEN saving THEN the date is past`() = runTest {
        val env = Environment()

        val gaps = env.gaps(
            StubTopic.stubContent(releaseDate = LocalDate.of(2026, 10, 22)),
            publishedDate = LocalDate.of(2026, 10, 25),
            today = LocalDate.of(2026, 10, 23),
        )

        assertEquals(expected = setOf(TopicGap.PastDate), actual = gaps)
    }

    private class Environment {

        val characterRepository = StubCharacterRepository()
        val rule = TopicPublishRule(
            topicDraftRule = TopicDraftRule(
                getCharacterUseCase = GetCharacterUseCase(characterRepository = characterRepository),
            ),
        )

        suspend fun gaps(
            content: TopicContent,
            publishedDate: LocalDate? = null,
            today: LocalDate = StubTopic.RELEASE_DATE,
        ): Set<TopicGap> = rule.gaps(content = content, publishedDate = publishedDate, today = today)
    }
}
