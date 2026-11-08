package app.yap.server.core.llm

import app.yap.server.core.llm.deepseek.DeepSeekModelProvider
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.dsl.onClose

fun coreLlmModule(config: LlmConfig): Module = module {
    single { config }
    singleOf(::ProviderConnections)
    single(createdAtStart = true) {
        ModelProviders(
            providers = listOfNotNull(
                config.deepSeek?.let { deepSeek -> ModelProviderId.DEEPSEEK to DeepSeekModelProvider(deepSeek) },
            ).toMap(),
        )
    } onClose { providers -> providers?.close() }
}
