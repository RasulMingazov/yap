package app.yap.server.feature.topics.persistence

import app.yap.server.feature.topics.model.StubTopic
import app.yap.server.feature.topics.model.Topic
import app.yap.server.feature.topics.model.TopicContent
import app.yap.server.feature.topics.model.TopicId
import io.github.rasulmingazov.stubcall.StubCall0
import io.github.rasulmingazov.stubcall.StubCall1
import io.github.rasulmingazov.stubcall.StubCall2
import java.time.LocalDate

class StubTopicRepository(
    topic: Topic = StubTopic.stubTopic(),
) : TopicRepository {

    val createCall = StubCall1.returns<TopicContent, Topic>(topic)
    val findCall = StubCall1.returns<TopicId, Topic?>(topic)
    val hasPublishedOnCall = StubCall2.returns<LocalDate, TopicId, Boolean>(false)
    val listCall = StubCall0.returns<List<Topic>>(listOf(topic))
    val publishCall = StubCall2.returns<TopicContent, TopicId, TopicWrite>(TopicWrite.Written(topic))
    val updateCall = StubCall1.returns<Topic, TopicWrite>(TopicWrite.Written(topic))

    override suspend fun create(content: TopicContent): Topic = createCall.invoke(content)

    override suspend fun find(topicId: TopicId): Topic? = findCall.invoke(topicId)

    override suspend fun hasPublishedOn(date: LocalDate, exceptTopicId: TopicId): Boolean =
        hasPublishedOnCall.invoke(date, exceptTopicId)

    override suspend fun list(): List<Topic> = listCall.invoke()

    override suspend fun publish(content: TopicContent, topicId: TopicId): TopicWrite =
        publishCall.invoke(content, topicId)

    override suspend fun update(topic: Topic): TopicWrite = updateCall.invoke(topic)
}
