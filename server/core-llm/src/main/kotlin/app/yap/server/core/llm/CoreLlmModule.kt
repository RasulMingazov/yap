package app.yap.server.core.llm

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun coreLlmModule(config: LlmConfig): Module = module {
    single { config }
    singleOf(::ProviderConnections)
}
