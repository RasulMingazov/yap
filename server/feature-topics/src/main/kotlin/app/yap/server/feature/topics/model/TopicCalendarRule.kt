package app.yap.server.feature.topics.model

import app.yap.server.feature.characters.model.Character
import app.yap.server.feature.characters.model.CharacterId
import java.time.LocalDate

class TopicCalendarRule(
    private val topicPublishRule: TopicPublishRule,
) {

    suspend fun calendar(
        characters: List<Character>,
        today: LocalDate,
        topics: List<Topic>,
    ): TopicCalendar {
        val names = characters.associate { character -> character.characterId to character.profile.name }
        val ordered = topics.sortedWith(
            compareBy<Topic> { topic -> topic.date }
                .thenBy(String.CASE_INSENSITIVE_ORDER) { topic -> topic.content.title },
        )
        val emptyDays = emptyDays(today, ordered)
        return TopicCalendar(
            rows = rows(emptyDays, names, today, ordered),
            todos = todos(emptyDays, today, ordered),
        )
    }

    private fun emptyDays(today: LocalDate, topics: List<Topic>): List<LocalDate> = (0 until DAYS)
        .map { offset -> today.plusDays(offset.toLong()) }
        .filterNot { day -> topics.any { topic -> topic.date == day } }

    private fun rows(
        emptyDays: List<LocalDate>,
        names: Map<CharacterId, String>,
        today: LocalDate,
        topics: List<Topic>,
    ): List<CalendarRow> {
        val topicRows = topics.map { topic ->
            CalendarRow(
                date = topic.date,
                topic = calendarTopic(names, today, topic),
            )
        }
        val emptyRows = emptyDays.map { day -> CalendarRow(date = day, topic = null) }
        return (topicRows + emptyRows).sortedBy { row -> row.date }
    }

    private fun calendarTopic(
        names: Map<CharacterId, String>,
        today: LocalDate,
        topic: Topic,
    ): CalendarTopic = CalendarTopic(
        completeSteps = topic.content.steps.count { step -> step.isComplete },
        ledBy = ledBy(topic.content.interlocutor, names),
        state = ReleaseState.of(topic.status, topic.date, today),
        steps = topic.content.steps.size,
        title = topic.content.title,
        topicId = topic.topicId,
    )

    private fun ledBy(interlocutor: Interlocutor?, names: Map<CharacterId, String>): LedBy? = when (interlocutor) {
        null -> null
        is Interlocutor.Cast -> names[interlocutor.characterId]?.let(LedBy::Cast)
        is Interlocutor.Guest -> LedBy.Guest
    }

    private suspend fun todos(
        emptyDays: List<LocalDate>,
        today: LocalDate,
        topics: List<Topic>,
    ): List<Todo> {
        val publishedDays = topics
            .filter { topic -> topic.status == TopicStatus.PUBLISHED }
            .map { topic -> topic.date }
            .toSet()
        val drafts = topics
            .filter { topic -> !topic.date.isBefore(today) && topic.date !in publishedDays }
            .map { topic ->
                Todo.Draft(
                    date = topic.date,
                    gaps = topicPublishRule.gaps(topic.content, publishedDate = null, today = today),
                    title = topic.content.title,
                    topicId = topic.topicId,
                )
            }
        val days = emptyDays.map(Todo::EmptyDay)
        return (drafts + days).sortedBy { todo -> todo.date }
    }

    private val Topic.date: LocalDate
        get() = requireNotNull(content.releaseDate) { "A stored topic has a release date" }
}

private const val DAYS = 14
