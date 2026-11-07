package app.yap.server.feature.topics.persistence

import app.yap.server.feature.topics.model.StubTopic
import app.yap.server.feature.topics.model.Topic
import app.yap.server.feature.topics.model.TopicContent
import app.yap.server.feature.topics.model.TopicId
import io.github.rasulmingazov.stubcall.StubCall0
import io.github.rasulmingazov.stubcall.StubCall1

class StubTopicRepository(
    topic: Topic = StubTopic.stubTopic(),
) : TopicRepository {

    val createCall = StubCall1.returns<TopicContent, Topic>(topic)
    val findCall = StubCall1.returns<TopicId, Topic?>(topic)
    val listCall = StubCall0.returns<List<Topic>>(listOf(topic))
    val updateCall = StubCall1.returns<Topic, Boolean>(true)

    override suspend fun create(content: TopicContent): Topic = createCall.invoke(content)

    override suspend fun find(topicId: TopicId): Topic? = findCall.invoke(topicId)

    override suspend fun list(): List<Topic> = listCall.invoke()

    override suspend fun update(topic: Topic): Boolean = updateCall.invoke(topic)
}
