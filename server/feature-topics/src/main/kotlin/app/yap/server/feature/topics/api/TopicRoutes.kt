package app.yap.server.feature.topics.api

import app.yap.contract.admin.TopicContentDto
import app.yap.contract.admin.TopicRejectionDto
import app.yap.server.feature.topics.model.CreateTopicOutcome
import app.yap.server.feature.topics.model.PublishTopicOutcome
import app.yap.server.feature.topics.model.TopicGap
import app.yap.server.feature.topics.model.TopicId
import app.yap.server.feature.topics.model.UpdateTopicOutcome
import app.yap.server.feature.topics.usecase.CreateTopicUseCase
import app.yap.server.feature.topics.usecase.GetTopicCalendarUseCase
import app.yap.server.feature.topics.usecase.GetTopicUseCase
import app.yap.server.feature.topics.usecase.PublishTopicUseCase
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
    getTopicCalendarUseCase: GetTopicCalendarUseCase,
    getTopicUseCase: GetTopicUseCase,
    publishTopicUseCase: PublishTopicUseCase,
    updateTopicUseCase: UpdateTopicUseCase,
) {
    route("/admin/topics") {
        getTopicCalendarRoute(getTopicCalendarUseCase)
        createTopicRoute(createTopicUseCase)
        getTopicRoute(getTopicUseCase)
        updateTopicRoute(updateTopicUseCase)
        publishTopicRoute(publishTopicUseCase)
    }
}

private fun Route.getTopicCalendarRoute(getTopicCalendarUseCase: GetTopicCalendarUseCase) {
    get("/calendar") {
        val calendar = getTopicCalendarUseCase()
        call.respond(calendar.toDto())
    }
}

private fun Route.createTopicRoute(createTopicUseCase: CreateTopicUseCase) {
    post {
        when (val outcome = createTopicUseCase(call.receive<TopicContentDto>().toModel())) {
            is CreateTopicOutcome.Created -> call.respond(HttpStatusCode.Created, outcome.details.toDto())
            is CreateTopicOutcome.Invalid -> call.respondRejected(outcome.gaps)
        }
    }
}

private fun Route.getTopicRoute(getTopicUseCase: GetTopicUseCase) {
    get("/{topicId}") {
        val details = getTopicUseCase(call.topicId())
        if (details == null) {
            call.respond(HttpStatusCode.NotFound)
        } else {
            call.respond(details.toDto())
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
            is UpdateTopicOutcome.Invalid -> call.respondRejected(outcome.gaps)
            is UpdateTopicOutcome.NotFound -> call.respond(HttpStatusCode.NotFound)
            is UpdateTopicOutcome.DateTaken -> call.respond(HttpStatusCode.Conflict)
            is UpdateTopicOutcome.Updated -> call.respond(outcome.details.toDto())
        }
    }
}

private fun Route.publishTopicRoute(publishTopicUseCase: PublishTopicUseCase) {
    post("/{topicId}/publish") {
        val dto = call.receive<TopicContentDto>()
        val outcome = publishTopicUseCase(
            content = dto.toModel(),
            topicId = call.topicId(),
        )
        when (outcome) {
            is PublishTopicOutcome.DateTaken -> call.respond(HttpStatusCode.Conflict)
            is PublishTopicOutcome.Invalid -> call.respondRejected(outcome.gaps)
            is PublishTopicOutcome.NotFound -> call.respond(HttpStatusCode.NotFound)
            is PublishTopicOutcome.Published -> call.respond(outcome.details.toDto())
        }
    }
}

private suspend fun RoutingCall.respondRejected(gaps: Set<TopicGap>) {
    respond(HttpStatusCode.BadRequest, TopicRejectionDto(gaps = gaps.toDto()))
}

private fun RoutingCall.topicId(): TopicId = parameters["topicId"]
    ?.let { value -> Uuid.parseOrNull(value) }
    ?.let(::TopicId)
    ?: throw NotFoundException()
