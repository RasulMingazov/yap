package app.yap.server.feature.topics.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class StepTest {

    @Test
    fun `GIVEN a step with no text WHEN checking the step THEN it is empty`() {
        val step = StubTopic.stubStep(outcome = "", passWhen = "", task = "")

        assertTrue(step.isEmpty)
    }

    @Test
    fun `GIVEN a step with only a task WHEN checking the step THEN it is not empty`() {
        val step = StubTopic.stubStep(outcome = "", passWhen = "", task = StubTopic.TASK)

        assertFalse(step.isEmpty)
    }
}
