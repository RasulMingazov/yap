package app.yap.admin.feature.topics.data.mapper

import app.yap.admin.feature.characters.domain.StubCharacter
import app.yap.admin.feature.topics.domain.StubTopic
import app.yap.contract.admin.InterlocutorDto
import app.yap.contract.admin.OpeningLineDto
import app.yap.contract.admin.StepDto
import app.yap.contract.admin.TopicContentDto
import app.yap.contract.admin.TopicDto

internal object StubTopicDto {

    const val RELEASE_DATE = "2025-10-24"

    fun stubTopicDto(
        content: TopicContentDto = stubContentDto(),
        topicId: String = StubTopic.TOPIC_ID,
    ): TopicDto = TopicDto(
        content = content,
        topicId = topicId,
    )

    fun stubContentDto(
        description: String = StubTopic.DESCRIPTION,
        durationMinutes: Int? = StubTopic.DURATION_MINUTES,
        interlocutor: InterlocutorDto? = InterlocutorDto.CastDto(characterId = StubCharacter.CHARACTER_ID),
        openingLine: OpeningLineDto = stubOpeningLineDto(),
        releaseDate: String? = RELEASE_DATE,
        role: String = StubTopic.ROLE,
        setting: String = StubTopic.SETTING,
        steps: List<StepDto> = listOf(stubStepDto()),
        title: String = StubTopic.TITLE,
    ): TopicContentDto = TopicContentDto(
        description = description,
        durationMinutes = durationMinutes,
        interlocutor = interlocutor,
        openingLine = openingLine,
        releaseDate = releaseDate,
        role = role,
        setting = setting,
        steps = steps,
        title = title,
    )

    fun stubOpeningLineDto(
        english: String = StubTopic.ENGLISH,
        translation: String = StubTopic.TRANSLATION,
    ): OpeningLineDto = OpeningLineDto(
        english = english,
        translation = translation,
    )

    fun stubStepDto(
        outcome: String = StubTopic.OUTCOME,
        passWhen: String = StubTopic.PASS_WHEN,
        task: String = StubTopic.TASK,
    ): StepDto = StepDto(
        outcome = outcome,
        passWhen = passWhen,
        task = task,
    )
}
