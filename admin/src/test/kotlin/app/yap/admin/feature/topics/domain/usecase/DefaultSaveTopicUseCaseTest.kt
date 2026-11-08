package app.yap.admin.feature.topics.domain.usecase

import app.yap.admin.feature.topics.domain.StubTopic
import app.yap.admin.feature.topics.domain.repository.StubTopicRepository
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

internal class DefaultSaveTopicUseCaseTest {

    @Test
    fun `GIVEN a new topic WHEN saving it THEN the content is created`() = runTest {
        val env = Environment()

        env.useCase(topicId = null, content = StubTopic.stubContent())

        env.topicRepository.createCall.calledWith(StubTopic.stubContent())
    }

    @Test
    fun `GIVEN a saved topic WHEN saving it THEN the topic is updated`() = runTest {
        val env = Environment()
        val topic = StubTopic.stubTopic()

        env.useCase(topicId = topic.topicId, content = topic.content)

        env.topicRepository.updateCall.calledWith(topic.topicId, topic.content)
    }

    private class Environment {

        val topicRepository = StubTopicRepository()
        val useCase = DefaultSaveTopicUseCase(topicRepository = topicRepository)
    }
}
