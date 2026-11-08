package app.yap.server.feature.models.model

import kotlin.test.Test
import kotlin.test.assertEquals

internal class ModelTaskTest {

    @Test
    fun `GIVEN the tasks WHEN listing them THEN they are Character reply, Step check, Hints, Review and Calibration`() {
        val tasks = ModelTask.entries

        assertEquals(
            expected = listOf(
                ModelTask.TOPIC_REPLY,
                ModelTask.TOPIC_CHECK,
                ModelTask.TOPIC_HINTS,
                ModelTask.TOPIC_REVIEW,
                ModelTask.CALIBRATION,
            ),
            actual = tasks,
        )
    }
}
