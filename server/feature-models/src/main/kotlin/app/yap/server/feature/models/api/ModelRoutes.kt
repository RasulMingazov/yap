package app.yap.server.feature.models.api

import app.yap.contract.admin.TaskModelDto
import app.yap.server.feature.models.model.SaveTaskModelsOutcome
import app.yap.server.feature.models.usecase.GetModelsUseCase
import app.yap.server.feature.models.usecase.SaveModelsUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.modelRoutes(
    getModelsUseCase: GetModelsUseCase,
    saveModelsUseCase: SaveModelsUseCase,
) {
    route("/admin/models") {
        getModelsRoute(getModelsUseCase)
        saveModelsRoute(saveModelsUseCase)
    }
}

private fun Route.getModelsRoute(getModelsUseCase: GetModelsUseCase) {
    get {
        call.respond(
            getModelsUseCase()
                .toDto(),
        )
    }
}

private fun Route.saveModelsRoute(saveModelsUseCase: SaveModelsUseCase) {
    put {
        val models = call
            .receive<List<TaskModelDto>>()
            .toModels()
            ?: return@put call.respond(HttpStatusCode.Conflict)
        when (val outcome = saveModelsUseCase(models)) {
            is SaveTaskModelsOutcome.CatalogChanged -> call.respond(HttpStatusCode.Conflict)
            is SaveTaskModelsOutcome.Invalid -> call.respond(HttpStatusCode.BadRequest)
            is SaveTaskModelsOutcome.Saved -> call.respond(outcome.overview.toDto())
        }
    }
}
