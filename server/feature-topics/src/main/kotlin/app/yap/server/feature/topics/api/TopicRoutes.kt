package app.yap.server.feature.topics.api

import app.yap.contract.admin.TopicContentDto
import app.yap.server.feature.topics.model.CreateTopicOutcome
import app.yap.server.feature.topics.model.TopicId
import app.yap.server.feature.topics.model.UpdateTopicOutcome
import app.yap.server.feature.topics.usecase.CreateTopicUseCase
import app.yap.server.feature.topics.usecase.GetTopicUseCase
import app.yap.server.feature.topics.usecase.GetTopicsUseCase
import app.yap.server.feature.topics.usecase.UpdateTopicUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import kotlin.uuid.Uuid

fun Route.topicRoutes(
    createTopicUseCase: CreateTopicUseCase,
    getTopicUseCase: GetTopicUseCase,
    getTopicsUseCase: GetTopicsUseCase,
    updateTopicUseCase: UpdateTopicUseCase,
) {
    route("/admin/topics") {
        getTopicsRoute(getTopicsUseCase)
        createTopicRoute(createTopicUseCase)
        getTopicRoute(getTopicUseCase)
        updateTopicRoute(updateTopicUseCase)
    }
}

private fun Route.getTopicsRoute(getTopicsUseCase: GetTopicsUseCase) {
    get {
        call.respond(getTopicsUseCase().map { topic -> topic.toDto() })
    }
}

private fun Route.createTopicRoute(createTopicUseCase: CreateTopicUseCase) {
    post {
        when (val outcome = createTopicUseCase(call.receive<TopicContentDto>().toModel())) {
            is CreateTopicOutcome.Created -> call.respond(HttpStatusCode.Created, outcome.details.topic.toDto())
            is CreateTopicOutcome.Invalid -> call.respond(HttpStatusCode.BadRequest)
        }
    }
}

private fun Route.getTopicRoute(getTopicUseCase: GetTopicUseCase) {
    get("/{topicId}") {
        val details = getTopicUseCase(call.topicId())
        if (details == null) {
            call.respond(HttpStatusCode.NotFound)
        } else {
            call.respond(details.topic.toDto())
        }
    }
}

private fun Route.updateTopicRoute(updateTopicUseCase: UpdateTopicUseCase) {
    put("/{topicId}") {
        val content = call.receive<TopicContentDto>().toModel()
        val outcome = updateTopicUseCase(
            content = content,
            topicId = call.topicId(),
        )
        when (outcome) {
            is UpdateTopicOutcome.Invalid -> call.respond(HttpStatusCode.BadRequest)
            is UpdateTopicOutcome.NotFound -> call.respond(HttpStatusCode.NotFound)
            is UpdateTopicOutcome.DateTaken -> call.respond(HttpStatusCode.Conflict)
            is UpdateTopicOutcome.Updated -> call.respond(outcome.details.topic.toDto())
        }
    }
}

private fun RoutingCall.topicId(): TopicId = parameters["topicId"]
    ?.let { value -> Uuid.parseOrNull(value) }
    ?.let(::TopicId)
    ?: throw NotFoundException()
