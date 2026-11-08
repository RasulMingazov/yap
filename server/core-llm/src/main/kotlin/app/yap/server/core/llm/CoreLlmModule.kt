package app.yap.server.core.llm

import app.yap.server.core.llm.deepseek.DeepSeekModelProvider
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.dsl.onClose

fun coreLlmModule(config: LlmConfig): Module = module {
    single { config }
    single(createdAtStart = true) {
        ModelProviders(
            providers = buildMap {
                config.deepSeek?.let { deepSeek ->
                    put(ModelProviderId.DEEPSEEK, DeepSeekModelProvider(deepSeek))
                }
            },
        )
    } onClose { providers -> providers?.close() }
    singleOf(::ProviderConnections)
}
