package app.yap.server.feature.topics

import app.yap.server.feature.characters.usecase.CharacterTopicCounts
import app.yap.server.feature.topics.model.TopicCalendarRule
import app.yap.server.feature.topics.model.TopicDetailsRule
import app.yap.server.feature.topics.model.TopicDraftRule
import app.yap.server.feature.topics.model.TopicPublishRule
import app.yap.server.feature.topics.persistence.DatabaseCharacterTopicCounts
import app.yap.server.feature.topics.persistence.DatabaseTopicRepository
import app.yap.server.feature.topics.persistence.TopicRepository
import app.yap.server.feature.topics.usecase.CreateTopicUseCase
import app.yap.server.feature.topics.usecase.GetTopicCalendarUseCase
import app.yap.server.feature.topics.usecase.GetTopicUseCase
import app.yap.server.feature.topics.usecase.PublishTopicUseCase
import app.yap.server.feature.topics.usecase.UpdateTopicUseCase
import java.time.Clock
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun featureTopicsModule(): Module = module {
    single<Clock> { Clock.systemUTC() }
    singleOf(::DatabaseCharacterTopicCounts) { bind<CharacterTopicCounts>() }
    singleOf(::DatabaseTopicRepository) { bind<TopicRepository>() }

    factoryOf(::CreateTopicUseCase)
    factoryOf(::GetTopicCalendarUseCase)
    factoryOf(::GetTopicUseCase)
    factoryOf(::PublishTopicUseCase)
    factoryOf(::TopicCalendarRule)
    factoryOf(::TopicDetailsRule)
    factoryOf(::TopicDraftRule)
    factoryOf(::TopicPublishRule)
    factoryOf(::UpdateTopicUseCase)
}
