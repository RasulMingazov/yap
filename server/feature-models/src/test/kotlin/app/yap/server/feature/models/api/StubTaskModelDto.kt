package app.yap.server.feature.models.api

import app.yap.contract.admin.TaskModelDto
import app.yap.server.feature.models.model.StubTaskModel

internal object StubTaskModelDto {

    fun stubTaskModelDto(
        model: String = "deepseek-flash",
        provider: String = "DEEPSEEK",
        task: String = "TOPIC_REPLY",
    ): TaskModelDto = TaskModelDto(
        model = model,
        provider = provider,
        task = task,
    )

    fun stubTableDtos(
        review: TaskModelDto = stubTaskModelDto(task = "TOPIC_REVIEW"),
    ): List<TaskModelDto> = listOf(
        stubTaskModelDto(task = "TOPIC_REPLY"),
        stubTaskModelDto(task = "TOPIC_CHECK"),
        stubTaskModelDto(task = "TOPIC_HINTS"),
        review,
        stubTaskModelDto(task = "CALIBRATION"),
    )

    fun stubReviewDto(
        model: String = StubTaskModel.PRO_MODEL,
    ): TaskModelDto = stubTaskModelDto(
        model = model,
        task = "TOPIC_REVIEW",
    )
}
