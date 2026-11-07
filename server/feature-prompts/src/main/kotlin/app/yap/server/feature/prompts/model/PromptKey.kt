package app.yap.server.feature.prompts.model

enum class PromptKey(
    val group: PromptGroup,
    val variables: List<String>,
) {
    TOPIC_REPLY(
        group = PromptGroup.TOPIC,
        variables = listOf(
            "{interlocutor}",
            "{topic.setting}",
            "{topic.role}",
            "{level.rules}",
            "{step.n}",
            "{step.task}",
            "{check.verdict}",
        ),
    ),
    TOPIC_CHECK(
        group = PromptGroup.TOPIC,
        variables = listOf("{step.task}", "{step.passWhen}", "{last.line}", "{answers}"),
    ),
    TOPIC_HINTS(
        group = PromptGroup.TOPIC,
        variables = listOf("{step.task}", "{topic.setting}", "{level.rules}", "{hint.level}"),
    ),
    TOPIC_REVIEW(
        group = PromptGroup.TOPIC,
        variables = listOf("{topic.title}", "{steps}", "{answers}"),
    ),
    CALIBRATION_CHECK(
        group = PromptGroup.CALIBRATION,
        variables = listOf("{step.task}", "{answer}"),
    ),
    CALIBRATION_REPLY(
        group = PromptGroup.CALIBRATION,
        variables = listOf("{step.n}", "{level.rules}", "{check.verdict}"),
    ),
}
