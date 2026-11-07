package app.yap.admin.core.network

import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.dsl.onClose

internal fun coreNetworkModule(): Module = module {
    single(createdAtStart = true) { adminHttpClient() } onClose { httpClient -> httpClient?.close() }
}
