package app.yap.admin.feature.topics

import app.yap.admin.feature.topics.data.repository.RemoteTopicRepository
import app.yap.admin.feature.topics.domain.repository.TopicRepository
import app.yap.admin.feature.topics.domain.usecase.DefaultGetTopicsUseCase
import app.yap.admin.feature.topics.domain.usecase.GetTopicsUseCase
import app.yap.admin.feature.topics.presentation.TopicsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal fun featureTopicsModule(): Module = module {
    singleOf(::RemoteTopicRepository) { bind<TopicRepository>() }

    factoryOf(::DefaultGetTopicsUseCase) { bind<GetTopicsUseCase>() }

    factoryOf(::TopicsViewModel)
}
