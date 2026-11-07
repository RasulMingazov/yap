package app.yap.server.feature.prompts.api

import app.yap.contract.admin.PromptDto
import app.yap.contract.admin.PromptTextDto
import app.yap.contract.admin.PromptVersionDto
import app.yap.server.feature.prompts.model.PromptKey
import app.yap.server.feature.prompts.model.StubPrompt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class PromptDtoMapperTest {

    @Test
    fun `GIVEN spaces and lines around the text WHEN mapping the text of a save THEN the text is trimmed`() {
        val dto = PromptTextDto(text = " \n Play {interlocutor}.\n ")

        val text = dto.toText()

        assertEquals(expected = "Play {interlocutor}.", actual = text)
    }

    @Test
    fun `GIVEN an older version that is current WHEN mapping the prompt THEN only that version is current`() {
        val prompt = StubPrompt.stubPrompt(
            current = StubPrompt.stubVersion(number = 1),
            promptKey = PromptKey.CALIBRATION_CHECK,
            versions = listOf(StubPrompt.stubVersion(number = 2), StubPrompt.stubVersion(number = 1)),
        )

        val dto = prompt.toDto()

        assertEquals(
            expected = PromptDto(
                group = "CALIBRATION",
                promptKey = "CALIBRATION_CHECK",
                text = StubPrompt.TEXT,
                variables = listOf("{step.task}", "{answer}"),
                versions = listOf(
                    PromptVersionDto(isCurrent = false, number = 2, savedAt = "2026-10-24T09:30:00Z"),
                    PromptVersionDto(isCurrent = true, number = 1, savedAt = "2026-10-24T09:30:00Z"),
                ),
            ),
            actual = dto,
        )
    }

    @Test
    fun `GIVEN a prompt that was never saved WHEN mapping it THEN the DTO has no text and no versions`() {
        val prompt = StubPrompt.stubPrompt(current = null, versions = emptyList())

        val dto = prompt.toDto()

        assertNull(dto.text)
        assertEquals(expected = emptyList(), actual = dto.versions)
    }
}
