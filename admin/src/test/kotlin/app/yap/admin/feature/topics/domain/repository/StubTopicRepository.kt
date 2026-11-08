package app.yap.admin.feature.topics.domain.repository

import app.yap.admin.core.common.LoadResult
import app.yap.admin.feature.topics.domain.LoadTopicResult
import app.yap.admin.feature.topics.domain.PublishTopicResult
import app.yap.admin.feature.topics.domain.SaveTopicResult
import app.yap.admin.feature.topics.domain.StubTopic
import app.yap.admin.feature.topics.domain.StubTopicCalendar
import app.yap.admin.feature.topics.domain.Topic
import app.yap.admin.feature.topics.domain.TopicCalendar
import app.yap.admin.feature.topics.domain.TopicContent
import app.yap.admin.feature.topics.domain.TopicId
import io.github.rasulmingazov.stubcall.StubCall0
import io.github.rasulmingazov.stubcall.StubCall1
import io.github.rasulmingazov.stubcall.StubCall2

internal class StubTopicRepository(
    topic: Topic = StubTopic.stubTopic(),
) : TopicRepository {

    val calendarCall = StubCall0.returns<LoadResult<TopicCalendar>>(LoadResult.Loaded(StubTopicCalendar.stubTopicCalendar()))
    val createCall = StubCall1.returns<TopicContent, SaveTopicResult>(SaveTopicResult.Saved(topic))
    val findCall = StubCall1.returns<TopicId, LoadTopicResult>(LoadTopicResult.Loaded(topic))
    val publishCall = StubCall2.returns<TopicId, TopicContent, PublishTopicResult>(PublishTopicResult.Published(topic))
    val updateCall = StubCall2.returns<TopicId, TopicContent, SaveTopicResult>(SaveTopicResult.Saved(topic))

    override suspend fun calendar(): LoadResult<TopicCalendar> = calendarCall.invoke()

    override suspend fun create(content: TopicContent): SaveTopicResult = createCall.invoke(content)

    override suspend fun find(topicId: TopicId): LoadTopicResult = findCall.invoke(topicId)

    override suspend fun publish(topicId: TopicId, content: TopicContent): PublishTopicResult =
        publishCall.invoke(topicId, content)

    override suspend fun update(topicId: TopicId, content: TopicContent): SaveTopicResult =
        updateCall.invoke(topicId, content)
}
