package app.yap.admin.feature.topics.domain.usecase

import app.yap.admin.core.common.LoadResult
import app.yap.admin.feature.topics.domain.StubTopicCalendar
import app.yap.admin.feature.topics.domain.TopicCalendar
import io.github.rasulmingazov.stubcall.StubCall0

internal class StubGetTopicCalendarUseCase(
    calendar: LoadResult<TopicCalendar> = LoadResult.Loaded(StubTopicCalendar.stubTopicCalendar()),
) : GetTopicCalendarUseCase {

    val invokeCall = StubCall0.returns<LoadResult<TopicCalendar>>(calendar)

    override suspend fun invoke(): LoadResult<TopicCalendar> = invokeCall.invoke()
}
