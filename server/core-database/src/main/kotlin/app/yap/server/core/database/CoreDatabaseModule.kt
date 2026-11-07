package app.yap.server.core.database

import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.dsl.onClose

fun coreDatabaseModule(config: DatabaseConfig): Module = module {
    single(createdAtStart = true) { openDatabase(config) } onClose { database -> database?.close() }
}
