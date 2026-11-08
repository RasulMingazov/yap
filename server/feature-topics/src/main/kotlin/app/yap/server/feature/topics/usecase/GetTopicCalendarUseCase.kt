package app.yap.server.feature.topics.usecase

import app.yap.server.feature.characters.usecase.GetCharactersUseCase
import app.yap.server.feature.topics.model.ReleaseState
import app.yap.server.feature.topics.model.TopicCalendar
import app.yap.server.feature.topics.model.TopicCalendarRule
import app.yap.server.feature.topics.persistence.TopicRepository
import java.time.Clock

class GetTopicCalendarUseCase(
    private val clock: Clock,
    private val getCharactersUseCase: GetCharactersUseCase,
    private val topicCalendarRule: TopicCalendarRule,
    private val topicRepository: TopicRepository,
) {

    suspend operator fun invoke(): TopicCalendar = topicCalendarRule.calendar(
        characters = getCharactersUseCase(),
        today = ReleaseState.today(clock),
        topics = topicRepository.list(),
    )
}
