package app.yap.admin.feature.topics.domain.usecase

import app.yap.admin.core.common.LoadResult
import app.yap.admin.feature.topics.domain.TopicCalendar
import app.yap.admin.feature.topics.domain.repository.TopicRepository

internal interface GetTopicCalendarUseCase {

    suspend operator fun invoke(): LoadResult<TopicCalendar>
}

internal class DefaultGetTopicCalendarUseCase(
    private val topicRepository: TopicRepository,
) : GetTopicCalendarUseCase {

    override suspend fun invoke(): LoadResult<TopicCalendar> = topicRepository.calendar()
}
