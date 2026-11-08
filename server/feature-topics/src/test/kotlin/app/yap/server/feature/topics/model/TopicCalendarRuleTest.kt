package app.yap.server.feature.topics.model

import app.yap.server.feature.characters.model.StubCharacter
import app.yap.server.feature.characters.persistence.StubCharacterRepository
import app.yap.server.feature.characters.usecase.GetCharacterUseCase
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

internal class TopicCalendarRuleTest {

    private val today = LocalDate.of(2026, 10, 23)
    private val fiveSteps = List(2) { StubTopic.stubStep() } + List(3) { StubTopic.stubStep(outcome = "") }

    @Test
    fun `GIVEN a topic of yesterday and a draft in two days WHEN building THEN every day has a row`() = runTest {
        val env = Environment()

        val calendar = env.rule.calendarOf(
            topicOn(today.minusDays(1), TopicStatus.PUBLISHED),
            topicOn(today.plusDays(2)),
        )

        assertEquals(expected = (-1L..13L).map(today::plusDays), actual = calendar.rows.map { row -> row.date })
    }

    @Test
    fun `GIVEN no topics WHEN building THEN the calendar has 14 empty days that are all to do`() = runTest {
        val env = Environment()

        val calendar = env.rule.calendarOf()

        assertEquals(expected = (0L..13L).map(today::plusDays), actual = calendar.rows.map { row -> row.date })
        assertEquals(
            expected = (0L..13L).map { offset -> Todo.EmptyDay(today.plusDays(offset)) },
            actual = calendar.todos,
        )
    }

    @Test
    fun `GIVEN a past draft and a draft without steps WHEN building THEN to do skips the past one`() = runTest {
        val env = Environment()

        val calendar = env.rule.calendarOf(
            topicOn(today.minusDays(1), content = StubTopic.stubContent(setting = "")),
            topicOn(today.plusDays(2), content = StubTopic.stubContent(steps = emptyList())),
        )

        assertEquals(
            expected = listOf(
                Todo.EmptyDay(today),
                Todo.EmptyDay(today.plusDays(1)),
                Todo.Draft(
                    date = today.plusDays(2),
                    gaps = setOf(TopicGap.Missing(TopicField.STEPS)),
                    title = StubTopic.TITLE,
                    topicId = StubTopic.TOPIC_ID,
                ),
            ) + (3L..13L).map { offset -> Todo.EmptyDay(today.plusDays(offset)) },
            actual = calendar.todos,
        )
    }

    @Test
    fun `GIVEN a published topic and a draft on one date WHEN building THEN to do skips the draft`() = runTest {
        val env = Environment()
        val date = today.plusDays(1)

        val calendar = env.rule.calendarOf(
            topicOn(date, TopicStatus.PUBLISHED, StubTopic.stubContent(title = "A")),
            topicOn(date, content = StubTopic.stubContent(title = "B")),
        )

        assertEquals(
            expected = listOf(ReleaseState.PUBLISHED, ReleaseState.DRAFT),
            actual = calendar.rows.mapNotNull { row -> row.topic?.state },
        )
        assertEquals(expected = emptyList(), actual = calendar.todos.filterIsInstance<Todo.Draft>())
    }

    @Test
    fun `GIVEN five steps and three without an outcome WHEN building THEN the row counts them`() = runTest {
        val env = Environment()

        val calendar = env.rule.calendarOf(topicOn(today, content = StubTopic.stubContent(steps = fiveSteps)))

        val topic = calendar.rows.mapNotNull { row -> row.topic }.single()
        assertEquals(expected = 5, actual = topic.steps)
        assertEquals(expected = 2, actual = topic.completeSteps)
    }

    @Test
    fun `GIVEN no description and the steps 3 to 5 incomplete WHEN building THEN to do names them`() = runTest {
        val env = Environment()

        val calendar = env.rule.calendarOf(
            topicOn(today, content = StubTopic.stubContent(description = "", steps = fiveSteps)),
        )

        assertEquals(
            expected = setOf(
                TopicGap.Missing(TopicField.DESCRIPTION),
                TopicGap.IncompleteSteps(listOf(3, 4, 5)),
            ),
            actual = calendar.todos.filterIsInstance<Todo.Draft>().single().gaps,
        )
    }

    @Test
    fun `GIVEN topics of one date WHEN building THEN the rows follow the titles ignoring case`() = runTest {
        val env = Environment()

        val calendar = env.rule.calendarOf(
            topicOn(today, content = StubTopic.stubContent(title = "Banana")),
            topicOn(today, content = StubTopic.stubContent(title = "apple")),
        )

        assertEquals(
            expected = listOf("apple", "Banana"),
            actual = calendar.rows.mapNotNull { row -> row.topic?.title },
        )
    }

    @Test
    fun `GIVEN a character and a guest WHEN building THEN the rows name the character and the guest`() = runTest {
        val env = Environment()
        val guest = Interlocutor.Guest("A tourist")

        val calendar = env.rule.calendarOf(
            topicOn(today, content = StubTopic.stubContent(title = "A")),
            topicOn(today, content = StubTopic.stubContent(interlocutor = guest, title = "B")),
        )

        assertEquals(
            expected = listOf(LedBy.Cast(StubCharacter.NAME), LedBy.Guest),
            actual = calendar.rows.mapNotNull { row -> row.topic?.ledBy },
        )
    }

    private fun topicOn(
        date: LocalDate,
        status: TopicStatus = TopicStatus.DRAFT,
        content: TopicContent = StubTopic.stubContent(),
    ): Topic = StubTopic.stubTopic(
        content = content.copy(releaseDate = date),
        status = status,
    )

    private suspend fun TopicCalendarRule.calendarOf(vararg topics: Topic): TopicCalendar = calendar(
        characters = listOf(StubCharacter.stubCharacter()),
        today = today,
        topics = topics.toList(),
    )

    private class Environment {

        val rule = TopicCalendarRule(
            topicPublishRule = TopicPublishRule(
                topicDraftRule = TopicDraftRule(
                    getCharacterUseCase = GetCharacterUseCase(characterRepository = StubCharacterRepository()),
                ),
            ),
        )
    }
}
