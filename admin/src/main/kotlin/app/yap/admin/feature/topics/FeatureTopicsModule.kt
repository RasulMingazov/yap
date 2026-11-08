package app.yap.admin.feature.topics

import app.yap.admin.feature.topics.data.api.createTopicApi
import app.yap.admin.feature.topics.data.repository.RemoteTopicRepository
import app.yap.admin.feature.topics.domain.TopicId
import app.yap.admin.feature.topics.domain.repository.TopicRepository
import app.yap.admin.feature.topics.domain.usecase.DefaultGetTopicCalendarUseCase
import app.yap.admin.feature.topics.domain.usecase.DefaultGetTopicUseCase
import app.yap.admin.feature.topics.domain.usecase.DefaultSaveTopicUseCase
import app.yap.admin.feature.topics.domain.usecase.GetTopicCalendarUseCase
import app.yap.admin.feature.topics.domain.usecase.GetTopicUseCase
import app.yap.admin.feature.topics.domain.usecase.SaveTopicUseCase
import app.yap.admin.feature.topics.presentation.TopicViewModel
import app.yap.admin.feature.topics.presentation.TopicsViewModel
import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal fun featureTopicsModule(): Module = module {
    single { get<Ktorfit>().createTopicApi() }
    singleOf(::RemoteTopicRepository) { bind<TopicRepository>() }

    factoryOf(::DefaultGetTopicCalendarUseCase) { bind<GetTopicCalendarUseCase>() }
    factoryOf(::DefaultGetTopicUseCase) { bind<GetTopicUseCase>() }
    factoryOf(::DefaultSaveTopicUseCase) { bind<SaveTopicUseCase>() }

    factory { (topicId: TopicId?) ->
        TopicViewModel(
            getCharactersUseCase = get(),
            getTopicUseCase = get(),
            saveTopicUseCase = get(),
            topicId = topicId,
        )
    }
    factoryOf(::TopicsViewModel)
}
