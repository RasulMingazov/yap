package app.yap.server.feature.prompts.model

import kotlin.test.Test
import kotlin.test.assertEquals

internal class PromptKeyTest {

    @Test
    fun `GIVEN the six prompts WHEN listing the keys THEN they come in the order of the spec with their groups`() {
        val keys = PromptKey.entries.map { promptKey -> promptKey to promptKey.group }

        assertEquals(
            expected = listOf(
                PromptKey.TOPIC_REPLY to PromptGroup.TOPIC,
                PromptKey.TOPIC_CHECK to PromptGroup.TOPIC,
                PromptKey.TOPIC_HINTS to PromptGroup.TOPIC,
                PromptKey.TOPIC_REVIEW to PromptGroup.TOPIC,
                PromptKey.CALIBRATION_CHECK to PromptGroup.CALIBRATION,
                PromptKey.CALIBRATION_REPLY to PromptGroup.CALIBRATION,
            ),
            actual = keys,
        )
    }

    @Test
    fun `GIVEN the six prompts WHEN reading their variables THEN each prompt has the variables of the spec in order`() {
        val variables = PromptKey.entries.associateWith { promptKey -> promptKey.variables }

        assertEquals(
            expected = mapOf(
                PromptKey.TOPIC_REPLY to listOf(
                    "{interlocutor}",
                    "{topic.setting}",
                    "{topic.role}",
                    "{level.rules}",
                    "{step.n}",
                    "{step.task}",
                    "{check.verdict}",
                ),
                PromptKey.TOPIC_CHECK to listOf("{step.task}", "{step.passWhen}", "{last.line}", "{answers}"),
                PromptKey.TOPIC_HINTS to listOf("{step.task}", "{topic.setting}", "{level.rules}", "{hint.level}"),
                PromptKey.TOPIC_REVIEW to listOf("{topic.title}", "{steps}", "{answers}"),
                PromptKey.CALIBRATION_CHECK to listOf("{step.task}", "{answer}"),
                PromptKey.CALIBRATION_REPLY to listOf("{step.n}", "{level.rules}", "{check.verdict}"),
            ),
            actual = variables,
        )
    }
}
