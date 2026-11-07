package app.yap.admin.core.network

import app.yap.admin.core.common.LoadResult
import app.yap.admin.core.common.runSuspendCatching
import app.yap.admin.feature.server.domain.ServerSettings
import app.yap.admin.feature.server.domain.repository.ServerSettingsRepository
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

internal suspend fun <T> ServerSettingsRepository.callServer(
    failed: T,
    read: suspend (HttpResponse) -> T,
    send: suspend (ServerSettings) -> HttpResponse,
): T {
    val settings = get() ?: return failed
    return runSuspendCatching { read(send(settings)) }
        .getOrElse { failed }
}

internal suspend fun <T> HttpResponse.toLoadResult(read: suspend HttpResponse.() -> T): LoadResult<T> =
    if (status == HttpStatusCode.OK) LoadResult.Loaded(read()) else LoadResult.Failed

internal fun ServerSettings.url(path: String): String = "${address.trimEnd('/')}$path"
