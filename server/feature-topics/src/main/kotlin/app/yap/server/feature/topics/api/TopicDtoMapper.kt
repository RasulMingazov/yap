package app.yap.server.feature.topics.api

import app.yap.contract.admin.InterlocutorDto
import app.yap.contract.admin.OpeningLineDto
import app.yap.contract.admin.StepDto
import app.yap.contract.admin.TopicContentDto
import app.yap.contract.admin.TopicDto
import app.yap.server.feature.characters.model.CharacterId
import app.yap.server.feature.topics.model.Interlocutor
import app.yap.server.feature.topics.model.OpeningLine
import app.yap.server.feature.topics.model.Step
import app.yap.server.feature.topics.model.Topic
import app.yap.server.feature.topics.model.TopicContent
import io.ktor.server.plugins.BadRequestException
import java.time.LocalDate
import java.time.format.DateTimeParseException
import kotlin.uuid.Uuid

internal fun Topic.toDto(): TopicDto = TopicDto(
    content = content.toDto(),
    topicId = topicId.value.toString(),
)

private fun TopicContent.toDto(): TopicContentDto = TopicContentDto(
    description = description,
    durationMinutes = durationMinutes,
    interlocutor = interlocutor?.toDto(),
    openingLine = openingLine.toDto(),
    releaseDate = releaseDate?.toString(),
    role = role,
    setting = setting,
    steps = steps.map { step -> step.toDto() },
    title = title,
)

private fun Interlocutor.toDto(): InterlocutorDto = when (this) {
    is Interlocutor.Cast -> InterlocutorDto.CastDto(characterId = characterId.value.toString())
    is Interlocutor.Guest -> InterlocutorDto.GuestDto(description = description)
}

private fun OpeningLine.toDto(): OpeningLineDto = OpeningLineDto(
    english = english,
    translation = translation,
)

private fun Step.toDto(): StepDto = StepDto(
    outcome = outcome,
    passWhen = passWhen,
    task = task,
)

internal fun TopicContentDto.toModel(): TopicContent = TopicContent(
    description = description,
    durationMinutes = durationMinutes,
    interlocutor = interlocutor?.toModel(),
    openingLine = openingLine.toModel(),
    releaseDate = releaseDate?.toReleaseDate(),
    role = role,
    setting = setting,
    steps = steps.map { step -> step.toModel() },
    title = title,
)

private fun InterlocutorDto.toModel(): Interlocutor = when (this) {
    is InterlocutorDto.CastDto -> Interlocutor.Cast(characterId = characterId.toCharacterId())
    is InterlocutorDto.GuestDto -> Interlocutor.Guest(description = description)
}

private fun String.toCharacterId(): CharacterId = Uuid.parseOrNull(this)
    ?.let(::CharacterId)
    ?: throw BadRequestException("The character is not an identifier")

private fun OpeningLineDto.toModel(): OpeningLine = OpeningLine(
    english = english,
    translation = translation,
)

private fun String.toReleaseDate(): LocalDate = try {
    LocalDate.parse(this)
} catch (exception: DateTimeParseException) {
    throw BadRequestException("The release date is not a date", exception)
}

private fun StepDto.toModel(): Step = Step(
    outcome = outcome,
    passWhen = passWhen,
    task = task,
)
