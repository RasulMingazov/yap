package app.yap.server.feature.levels.api

import app.yap.contract.admin.LevelInputDto
import app.yap.server.feature.levels.model.SaveLevelsOutcome
import app.yap.server.feature.levels.usecase.GetLevelsUseCase
import app.yap.server.feature.levels.usecase.SaveLevelsUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.levelRoutes(
    getLevelsUseCase: GetLevelsUseCase,
    saveLevelsUseCase: SaveLevelsUseCase,
) {
    route("/admin/levels") {
        getLevelsRoute(getLevelsUseCase)
        saveLevelsRoute(saveLevelsUseCase)
    }
}

private fun Route.getLevelsRoute(getLevelsUseCase: GetLevelsUseCase) {
    get {
        call.respond(getLevelsUseCase().map { level -> level.toDto() })
    }
}

private fun Route.saveLevelsRoute(saveLevelsUseCase: SaveLevelsUseCase) {
    put {
        val entries = call.receive<List<LevelInputDto>>().map { input -> input.toModel() }
        when (val outcome = saveLevelsUseCase(entries)) {
            is SaveLevelsOutcome.Invalid -> call.respond(HttpStatusCode.BadRequest)
            is SaveLevelsOutcome.Saved -> call.respond(outcome.levels.map { level -> level.toDto() })
        }
    }
}
