package app.yap.admin.feature.topics.domain.repository

import app.yap.admin.feature.topics.domain.LoadTopicResult
import app.yap.admin.feature.topics.domain.LoadTopicsResult
import app.yap.admin.feature.topics.domain.SaveTopicResult
import app.yap.admin.feature.topics.domain.StubTopic
import app.yap.admin.feature.topics.domain.Topic
import app.yap.admin.feature.topics.domain.TopicContent
import app.yap.admin.feature.topics.domain.TopicId
import io.github.rasulmingazov.stubcall.StubCall0
import io.github.rasulmingazov.stubcall.StubCall1

internal class StubTopicRepository(
    topic: Topic = StubTopic.stubTopic(),
) : TopicRepository {

    val createCall = StubCall1.returns<TopicContent, SaveTopicResult>(SaveTopicResult.Saved(topic))
    val findCall = StubCall1.returns<TopicId, LoadTopicResult>(LoadTopicResult.Loaded(topic))
    val listCall = StubCall0.returns<LoadTopicsResult>(LoadTopicsResult.Loaded(listOf(topic)))
    val updateCall = StubCall1.returns<Topic, SaveTopicResult>(SaveTopicResult.Saved(topic))

    override suspend fun create(content: TopicContent): SaveTopicResult = createCall.invoke(content)

    override suspend fun find(topicId: TopicId): LoadTopicResult = findCall.invoke(topicId)

    override suspend fun list(): LoadTopicsResult = listCall.invoke()

    override suspend fun update(topic: Topic): SaveTopicResult = updateCall.invoke(topic)
}
