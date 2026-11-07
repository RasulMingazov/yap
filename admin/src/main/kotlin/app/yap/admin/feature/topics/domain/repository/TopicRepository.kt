package app.yap.admin.feature.topics.domain.repository

import app.yap.admin.feature.topics.domain.LoadTopicResult
import app.yap.admin.feature.topics.domain.LoadTopicsResult
import app.yap.admin.feature.topics.domain.SaveTopicResult
import app.yap.admin.feature.topics.domain.Topic
import app.yap.admin.feature.topics.domain.TopicContent
import app.yap.admin.feature.topics.domain.TopicId

internal interface TopicRepository {

    suspend fun create(content: TopicContent): SaveTopicResult

    suspend fun find(topicId: TopicId): LoadTopicResult

    suspend fun list(): LoadTopicsResult

    suspend fun update(topic: Topic): SaveTopicResult
}
