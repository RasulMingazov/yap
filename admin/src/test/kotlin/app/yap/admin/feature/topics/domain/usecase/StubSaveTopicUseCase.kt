package app.yap.admin.feature.topics.domain.usecase

import app.yap.admin.feature.topics.domain.SaveTopicResult
import app.yap.admin.feature.topics.domain.StubTopic
import app.yap.admin.feature.topics.domain.TopicContent
import app.yap.admin.feature.topics.domain.TopicId
import io.github.rasulmingazov.stubcall.StubCall2

internal class StubSaveTopicUseCase(
    result: SaveTopicResult = SaveTopicResult.Saved(StubTopic.stubTopic()),
) : SaveTopicUseCase {

    val invokeCall = StubCall2.returns<TopicId?, TopicContent, SaveTopicResult>(result)

    override suspend fun invoke(topicId: TopicId?, content: TopicContent): SaveTopicResult =
        invokeCall.invoke(topicId, content)
}
