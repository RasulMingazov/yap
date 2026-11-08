package app.yap.server.feature.topics.persistence

import app.yap.server.feature.topics.model.Topic
import app.yap.server.feature.topics.model.TopicContent
import app.yap.server.feature.topics.model.TopicId
import app.yap.server.feature.topics.model.UpdateTopicOutcome

interface TopicRepository {

    suspend fun create(content: TopicContent): Topic

    suspend fun find(topicId: TopicId): Topic?

    suspend fun list(): List<Topic>

    suspend fun update(topic: Topic): UpdateTopicOutcome
}
