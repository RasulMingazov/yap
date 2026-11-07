package app.yap.admin.feature.topics.data.mapper

import app.yap.admin.feature.characters.domain.CharacterId
import app.yap.admin.feature.topics.domain.Interlocutor
import app.yap.admin.feature.topics.domain.OpeningLine
import app.yap.admin.feature.topics.domain.Step
import app.yap.admin.feature.topics.domain.Topic
import app.yap.admin.feature.topics.domain.TopicContent
import app.yap.admin.feature.topics.domain.TopicId
import app.yap.contract.admin.InterlocutorDto
import app.yap.contract.admin.OpeningLineDto
import app.yap.contract.admin.StepDto
import app.yap.contract.admin.TopicContentDto
import app.yap.contract.admin.TopicDto
import java.time.LocalDate

internal fun TopicDto.toDomain(): Topic = Topic(
    content = content.toDomain(),
    topicId = TopicId(topicId),
)

private fun TopicContentDto.toDomain(): TopicContent = TopicContent(
    description = description,
    durationMinutes = durationMinutes,
    interlocutor = interlocutor?.toDomain(),
    openingLine = openingLine.toDomain(),
    releaseDate = releaseDate?.let(LocalDate::parse),
    role = role,
    setting = setting,
    steps = steps.map { step -> step.toDomain() },
    title = title,
)

private fun InterlocutorDto.toDomain(): Interlocutor = when (this) {
    is InterlocutorDto.CastDto -> Interlocutor.Cast(characterId = CharacterId(characterId))
    is InterlocutorDto.GuestDto -> Interlocutor.Guest(description = description)
}

private fun OpeningLineDto.toDomain(): OpeningLine = OpeningLine(
    english = english,
    translation = translation,
)

private fun StepDto.toDomain(): Step = Step(
    outcome = outcome,
    passWhen = passWhen,
    task = task,
)
