package app.yap.admin.feature.server.data.repository

import app.yap.admin.core.common.runSuspendCatching
import app.yap.admin.feature.server.data.mapper.toDomain
import app.yap.admin.feature.server.domain.ServerConnection
import app.yap.admin.feature.server.domain.ServerSettings
import app.yap.admin.feature.server.domain.repository.ServerRepository
import app.yap.contract.admin.ServerInfoDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

internal class DefaultServerRepository(
    private val httpClient: HttpClient,
) : ServerRepository {

    override suspend fun connect(settings: ServerSettings): ServerConnection = runSuspendCatching {
        httpClient
            .get("${settings.address.trimEnd('/')}/admin/server") { bearerAuth(settings.key) }
            .toConnection()
    }
        .getOrElse { ServerConnection.Unreachable }

    private suspend fun HttpResponse.toConnection(): ServerConnection = when (status) {
        HttpStatusCode.OK -> body<ServerInfoDto>().toDomain()
        HttpStatusCode.Unauthorized -> ServerConnection.Unauthorized
        else -> ServerConnection.Unreachable
    }
}
