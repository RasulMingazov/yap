package app.yap.server.feature.topics

import app.yap.server.feature.topics.model.TopicDraftRule
import app.yap.server.feature.topics.persistence.DatabaseTopicRepository
import app.yap.server.feature.topics.persistence.TopicRepository
import app.yap.server.feature.topics.usecase.CreateTopicUseCase
import app.yap.server.feature.topics.usecase.GetTopicUseCase
import app.yap.server.feature.topics.usecase.GetTopicsUseCase
import app.yap.server.feature.topics.usecase.UpdateTopicUseCase
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun featureTopicsModule(): Module = module {
    singleOf(::DatabaseTopicRepository) { bind<TopicRepository>() }

    factoryOf(::CreateTopicUseCase)
    factoryOf(::GetTopicUseCase)
    factoryOf(::GetTopicsUseCase)
    factoryOf(::TopicDraftRule)
    factoryOf(::UpdateTopicUseCase)
}
