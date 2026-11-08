package app.yap.admin.core.network

import app.yap.admin.core.common.LoadResult
import app.yap.admin.core.common.runSuspendCatching
import app.yap.admin.core.network.settings.ServerSettingsRepository
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

internal suspend fun <T> ServerSettingsRepository.callServer(
    failed: T,
    read: suspend (HttpResponse) -> T,
    send: suspend () -> HttpResponse,
): T {
    if (get() == null) return failed
    return runSuspendCatching { read(send()) }
        .getOrElse { failed }
}

internal suspend fun <T> HttpResponse.toLoadResult(read: suspend HttpResponse.() -> T): LoadResult<T> =
    if (status == HttpStatusCode.OK) LoadResult.Loaded(read()) else LoadResult.Failed
