package app.yap.server.admin

import app.yap.contract.admin.ServerInfoDto
import app.yap.server.config.AppEnvironment
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

internal fun Route.serverRoutes(environment: AppEnvironment) {
    get("/admin/server") {
        call.respond(ServerInfoDto(environment = environment.name.lowercase()))
    }
}
