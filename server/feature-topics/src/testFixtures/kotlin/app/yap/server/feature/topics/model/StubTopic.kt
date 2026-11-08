package app.yap.server.feature.topics.model

import app.yap.server.feature.characters.model.StubCharacter
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.uuid.Uuid

object StubTopic {

    const val DESCRIPTION = "Order a coffee and a sandwich."
    const val DURATION_MINUTES = 5
    const val ENGLISH = "Hi! What can I get you?"
    const val OUTCOME = "The learner has ordered a drink."
    const val PASS_WHEN = "The learner names the drink and the size."
    const val ROLE = "A barista at a busy cafe."
    const val SETTING = "A cafe in the morning."
    const val TASK = "Order a drink."
    const val TITLE = "At the cafe"
    const val TRANSLATION = "Привет! Что вам предложить?"
    val CLOCK: Clock = Clock.fixed(Instant.parse("2026-10-22T22:30:00Z"), ZoneOffset.UTC)
    val RELEASE_DATE: LocalDate = LocalDate.of(2026, 10, 24)
    val TOPIC_ID = TopicId(Uuid.parse("7d0c2b8e-4f31-4a6b-9c55-3e1f8a2d6b90"))

    fun stubTopic(
        content: TopicContent = stubContent(),
        status: TopicStatus = TopicStatus.DRAFT,
        topicId: TopicId = TOPIC_ID,
    ): Topic = Topic(
        content = content,
        status = status,
        topicId = topicId,
    )

    fun stubDetails(
        gaps: Set<TopicGap> = emptySet(),
        state: ReleaseState = ReleaseState.DRAFT,
        topic: Topic = stubTopic(),
    ): TopicDetails = TopicDetails(
        gaps = gaps,
        state = state,
        topic = topic,
    )

    fun stubCalendar(
        rows: List<CalendarRow> = emptyList(),
        todos: List<Todo> = emptyList(),
    ): TopicCalendar = TopicCalendar(
        rows = rows,
        todos = todos,
    )

    fun stubCalendarTopic(
        completeSteps: Int = 0,
        ledBy: LedBy? = null,
        state: ReleaseState = ReleaseState.DRAFT,
        steps: Int = 0,
        title: String = TITLE,
        topicId: TopicId = TOPIC_ID,
    ): CalendarTopic = CalendarTopic(
        completeSteps = completeSteps,
        ledBy = ledBy,
        state = state,
        steps = steps,
        title = title,
        topicId = topicId,
    )

    fun stubContent(
        description: String = DESCRIPTION,
        durationMinutes: Int? = DURATION_MINUTES,
        interlocutor: Interlocutor? = Interlocutor.Cast(characterId = StubCharacter.CHARACTER_ID),
        openingLine: OpeningLine = stubOpeningLine(),
        releaseDate: LocalDate? = RELEASE_DATE,
        role: String = ROLE,
        setting: String = SETTING,
        steps: List<Step> = listOf(stubStep()),
        title: String = TITLE,
    ): TopicContent = TopicContent(
        description = description,
        durationMinutes = durationMinutes,
        interlocutor = interlocutor,
        openingLine = openingLine,
        releaseDate = releaseDate,
        role = role,
        setting = setting,
        steps = steps,
        title = title,
    )

    fun stubPaddedContent(): TopicContent = stubContent(
        steps = listOf(stubStep(), stubStep(outcome = " ", passWhen = "", task = "")),
        title = " $TITLE ",
    )

    fun stubOpeningLine(
        english: String = ENGLISH,
        translation: String = TRANSLATION,
    ): OpeningLine = OpeningLine(
        english = english,
        translation = translation,
    )

    fun stubStep(
        outcome: String = OUTCOME,
        passWhen: String = PASS_WHEN,
        task: String = TASK,
    ): Step = Step(
        outcome = outcome,
        passWhen = passWhen,
        task = task,
    )
}
