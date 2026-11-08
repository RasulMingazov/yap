package app.yap.admin.feature.topics.domain.usecase

import app.yap.admin.feature.topics.domain.LoadTopicResult
import app.yap.admin.feature.topics.domain.StubTopic
import app.yap.admin.feature.topics.domain.TopicId
import io.github.rasulmingazov.stubcall.StubCall1

internal class StubGetTopicUseCase(
    topic: LoadTopicResult = LoadTopicResult.Loaded(StubTopic.stubTopic()),
) : GetTopicUseCase {

    val invokeCall = StubCall1.returns<TopicId, LoadTopicResult>(topic)

    override suspend fun invoke(topicId: TopicId): LoadTopicResult = invokeCall.invoke(topicId)
}
