package app.yap.admin.navigation

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

internal fun navigationModule(): Module = module {
    factoryOf(::NavigationViewModel)
}
