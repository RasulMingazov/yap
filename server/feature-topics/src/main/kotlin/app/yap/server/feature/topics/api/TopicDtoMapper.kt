package app.yap.server.feature.topics.api

import app.yap.contract.admin.InterlocutorDto
import app.yap.contract.admin.OpeningLineDto
import app.yap.contract.admin.StepDto
import app.yap.contract.admin.TopicContentDto
import app.yap.contract.admin.TopicDto
import app.yap.contract.admin.TopicFieldDto
import app.yap.contract.admin.TopicGapDto
import app.yap.contract.admin.TopicStatusDto
import app.yap.server.feature.characters.model.CharacterId
import app.yap.server.feature.topics.model.Interlocutor
import app.yap.server.feature.topics.model.OpeningLine
import app.yap.server.feature.topics.model.ReleaseState
import app.yap.server.feature.topics.model.Step
import app.yap.server.feature.topics.model.TopicContent
import app.yap.server.feature.topics.model.TopicDetails
import app.yap.server.feature.topics.model.TopicField
import app.yap.server.feature.topics.model.TopicGap
import io.ktor.server.plugins.BadRequestException
import java.time.LocalDate
import java.time.format.DateTimeParseException
import kotlin.uuid.Uuid

internal fun TopicDetails.toDto(): TopicDto = TopicDto(
    content = topic.content.toDto(),
    gaps = gaps.toDto(),
    status = state.toDto(),
    topicId = topic.topicId.value.toString(),
)

internal fun Set<TopicGap>.toDto(): List<TopicGapDto> = map { gap -> gap.toDto() }

internal fun ReleaseState.toDto(): TopicStatusDto = when (this) {
    ReleaseState.DRAFT -> TopicStatusDto.DRAFT
    ReleaseState.PUBLISHED -> TopicStatusDto.PUBLISHED
    ReleaseState.TODAY -> TopicStatusDto.TODAY
    ReleaseState.OUT -> TopicStatusDto.OUT
}

private fun TopicGap.toDto(): TopicGapDto = when (this) {
    is TopicGap.DateTaken -> TopicGapDto.DateTakenDto
    is TopicGap.IncompleteSteps -> TopicGapDto.IncompleteStepsDto(numbers = numbers)
    is TopicGap.Missing -> TopicGapDto.MissingDto(field = field.toDto())
    is TopicGap.PastDate -> TopicGapDto.PastDateDto
}

private fun TopicField.toDto(): TopicFieldDto = when (this) {
    TopicField.TITLE -> TopicFieldDto.TITLE
    TopicField.RELEASE_DATE -> TopicFieldDto.RELEASE_DATE
    TopicField.DURATION -> TopicFieldDto.DURATION
    TopicField.DESCRIPTION -> TopicFieldDto.DESCRIPTION
    TopicField.SETTING -> TopicFieldDto.SETTING
    TopicField.INTERLOCUTOR -> TopicFieldDto.INTERLOCUTOR
    TopicField.ROLE -> TopicFieldDto.ROLE
    TopicField.OPENING_LINE -> TopicFieldDto.OPENING_LINE
    TopicField.TRANSLATION -> TopicFieldDto.TRANSLATION
    TopicField.STEPS -> TopicFieldDto.STEPS
}

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
