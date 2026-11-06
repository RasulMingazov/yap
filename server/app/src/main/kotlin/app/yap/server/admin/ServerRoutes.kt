package app.yap.server.admin

import app.yap.contract.admin.ServerInfoDto
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

internal fun Route.serverRoutes(getServerEnvironmentUseCase: GetServerEnvironmentUseCase) {
    get("/admin/server") {
        val environment = getServerEnvironmentUseCase()
        call.respond(ServerInfoDto(environment = environment.name.lowercase()))
    }
}
