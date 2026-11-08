package app.yap.admin.feature.topics.domain.usecase

import app.yap.admin.feature.topics.domain.PublishTopicResult
import app.yap.admin.feature.topics.domain.StubTopic
import app.yap.admin.feature.topics.domain.repository.StubTopicRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

internal class DefaultPublishTopicUseCaseTest {

    @Test
    fun `GIVEN a draft WHEN publishing it THEN the repository publishes the content and its result is returned`() = runTest {
        val env = Environment()
        val topic = StubTopic.stubTopic()

        val result = env.useCase(topicId = topic.topicId, content = topic.content)

        env.topicRepository.publishCall.calledWith(topic.topicId, topic.content)
        assertEquals(expected = PublishTopicResult.Published(topic), actual = result)
    }

    private class Environment {

        val topicRepository = StubTopicRepository()
        val useCase = DefaultPublishTopicUseCase(topicRepository = topicRepository)
    }
}
