package app.yap.admin.feature.topics.domain.usecase

import app.yap.admin.feature.topics.domain.LoadTopicsResult
import app.yap.admin.feature.topics.domain.StubTopic
import io.github.rasulmingazov.stubcall.StubCall0

internal class StubGetTopicsUseCase(
    topics: LoadTopicsResult = LoadTopicsResult.Loaded(listOf(StubTopic.stubTopic())),
) : GetTopicsUseCase {

    val invokeCall = StubCall0.returns<LoadTopicsResult>(topics)

    override suspend fun invoke(): LoadTopicsResult = invokeCall.invoke()
}
