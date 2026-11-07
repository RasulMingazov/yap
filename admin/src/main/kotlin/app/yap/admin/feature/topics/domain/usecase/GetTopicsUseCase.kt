package app.yap.admin.feature.topics.domain.usecase

import app.yap.admin.feature.topics.domain.LoadTopicsResult
import app.yap.admin.feature.topics.domain.repository.TopicRepository

internal interface GetTopicsUseCase {

    suspend operator fun invoke(): LoadTopicsResult
}

internal class DefaultGetTopicsUseCase(
    private val topicRepository: TopicRepository,
) : GetTopicsUseCase {

    override suspend fun invoke(): LoadTopicsResult = topicRepository.list()
}
