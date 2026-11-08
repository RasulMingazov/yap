package app.yap.server.feature.topics.model

import app.yap.server.feature.characters.persistence.StubCharacterRepository
import app.yap.server.feature.characters.usecase.GetCharacterUseCase
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

internal class TopicDetailsRuleTest {

    private val today = moscowToday(StubTopic.CLOCK)
    private val rule = TopicDetailsRule(
        topicPublishRule = TopicPublishRule(
            topicDraftRule = TopicDraftRule(
                getCharacterUseCase = GetCharacterUseCase(characterRepository = StubCharacterRepository()),
            ),
        ),
    )

    @Test
    fun `GIVEN a draft with every field and a free date WHEN reading the details THEN it names nothing`() = runTest {
        val draft = StubTopic.stubTopic()

        val details = rule.details(isDateTaken = false, today = today, topic = draft)

        assertEquals(
            expected = TopicDetails(gaps = emptySet(), state = ReleaseState.DRAFT, topic = draft),
            actual = details,
        )
    }

    @Test
    fun `GIVEN a draft on a date with a published topic WHEN reading the details THEN it names the taken date`() =
        runTest {
            val details = rule.details(isDateTaken = true, today = today, topic = StubTopic.stubTopic())

            assertEquals(expected = setOf(TopicGap.DateTaken), actual = details.gaps)
        }

    @Test
    fun `GIVEN a draft with a past date WHEN reading the details THEN it names the past date`() = runTest {
        val content = StubTopic.stubContent(releaseDate = LocalDate.of(2026, 10, 22))

        val details = rule.details(isDateTaken = false, today = today, topic = StubTopic.stubTopic(content = content))

        assertEquals(expected = setOf(TopicGap.PastDate), actual = details.gaps)
    }

    @Test
    fun `GIVEN a published topic that is out WHEN reading the details THEN it keeps its past date`() = runTest {
        val content = StubTopic.stubContent(releaseDate = LocalDate.of(2026, 10, 22))
        val published = StubTopic.stubTopic(content = content, status = TopicStatus.PUBLISHED)

        val details = rule.details(isDateTaken = true, today = today, topic = published)

        assertEquals(
            expected = TopicDetails(gaps = emptySet(), state = ReleaseState.OUT, topic = published),
            actual = details,
        )
    }
}
