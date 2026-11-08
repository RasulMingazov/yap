package app.yap.server.feature.prompts.api

import app.yap.contract.admin.PromptTextDto
import app.yap.server.feature.prompts.model.PromptKey
import app.yap.server.feature.prompts.model.RestorePromptOutcome
import app.yap.server.feature.prompts.model.SavePromptOutcome
import app.yap.server.feature.prompts.usecase.GetPromptUseCase
import app.yap.server.feature.prompts.usecase.GetPromptsUseCase
import app.yap.server.feature.prompts.usecase.RestorePromptUseCase
import app.yap.server.feature.prompts.usecase.SavePromptUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.promptRoutes(
    getPromptUseCase: GetPromptUseCase,
    getPromptsUseCase: GetPromptsUseCase,
    restorePromptUseCase: RestorePromptUseCase,
    savePromptUseCase: SavePromptUseCase,
) {
    route("/admin/prompts") {
        getPromptsRoute(getPromptsUseCase)
        getPromptRoute(getPromptUseCase)
        savePromptRoute(savePromptUseCase)
        restorePromptRoute(restorePromptUseCase)
    }
}

private fun Route.getPromptsRoute(getPromptsUseCase: GetPromptsUseCase) {
    get {
        call.respond(getPromptsUseCase().map { promptKey -> promptKey.toSummaryDto() })
    }
}

private fun Route.getPromptRoute(getPromptUseCase: GetPromptUseCase) {
    get("/{promptKey}") {
        call.respond(getPromptUseCase(call.promptKey()).toDto())
    }
}

private fun Route.savePromptRoute(savePromptUseCase: SavePromptUseCase) {
    post("/{promptKey}/versions") {
        val text = call.receive<PromptTextDto>().text
        when (val outcome = savePromptUseCase(call.promptKey(), text)) {
            is SavePromptOutcome.EmptyText -> call.respond(HttpStatusCode.BadRequest)
            is SavePromptOutcome.Saved -> call.respond(HttpStatusCode.Created, outcome.prompt.toDto())
        }
    }
}

private fun Route.restorePromptRoute(restorePromptUseCase: RestorePromptUseCase) {
    post("/{promptKey}/versions/{number}/restore") {
        when (val outcome = restorePromptUseCase(call.promptKey(), call.versionNumber())) {
            is RestorePromptOutcome.NotFound -> call.respond(HttpStatusCode.NotFound)
            is RestorePromptOutcome.Restored -> call.respond(outcome.prompt.toDto())
        }
    }
}

private fun RoutingCall.promptKey(): PromptKey = parameters["promptKey"]
    ?.let { value -> PromptKey.entries.find { promptKey -> promptKey.name == value } }
    ?: throw NotFoundException()

private fun RoutingCall.versionNumber(): Int = parameters["number"]
    ?.toIntOrNull()
    ?: throw NotFoundException()
