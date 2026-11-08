package app.yap.server.feature.topics.persistence

import app.yap.server.feature.topics.model.Topic
import app.yap.server.feature.topics.model.TopicContent
import app.yap.server.feature.topics.model.TopicId
import java.time.LocalDate

interface TopicRepository {

    suspend fun create(content: TopicContent): Topic

    suspend fun find(topicId: TopicId): Topic?

    suspend fun hasPublishedOn(date: LocalDate, exceptTopicId: TopicId): Boolean

    suspend fun list(): List<Topic>

    suspend fun publish(content: TopicContent, topicId: TopicId): TopicPublish

    suspend fun update(topic: Topic): TopicWrite
}
