package app.yap.server.feature.topics.api

import app.yap.contract.admin.InterlocutorDto
import app.yap.contract.admin.OpeningLineDto
import app.yap.contract.admin.StepDto
import app.yap.contract.admin.TopicContentDto
import app.yap.contract.admin.TopicDto
import app.yap.server.feature.topics.model.StubTopic

internal object StubTopicDto {

    const val CHARACTER_ID = "2c7a5f1e-9a8b-4c3d-8e2f-1a2b3c4d5e6f"
    const val RELEASE_DATE = "2026-10-24"
    const val TOPIC_ID = "7d0c2b8e-4f31-4a6b-9c55-3e1f8a2d6b90"

    fun stubTopicDto(
        content: TopicContentDto = stubContentDto(),
        topicId: String = TOPIC_ID,
    ): TopicDto = TopicDto(
        content = content,
        topicId = topicId,
    )

    fun stubContentDto(
        description: String = StubTopic.DESCRIPTION,
        durationMinutes: Int? = StubTopic.DURATION_MINUTES,
        interlocutor: InterlocutorDto? = InterlocutorDto.CastDto(characterId = CHARACTER_ID),
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
