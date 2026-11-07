package app.yap.server.feature.characters.api

import app.yap.contract.admin.CharacterProfileDto
import app.yap.server.feature.characters.model.StubCharacter
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CharacterDtoMapperTest {

    @Test
    fun `GIVEN fields with spaces around WHEN mapping the profile THEN the fields are trimmed`() {
        val dto = CharacterProfileDto(
            history = " Moved here. ",
            manner = " Casual. ",
            name = " Sam ",
            personality = " Friendly. ",
        )

        val profile = dto.toModel()

        assertEquals(
            expected = StubCharacter.stubProfile(
                history = "Moved here.",
                manner = "Casual.",
                name = "Sam",
                personality = "Friendly.",
            ),
            actual = profile,
        )
    }

    @Test
    fun `GIVEN a field with spaces only WHEN mapping the profile THEN the field is empty`() {
        val dto = CharacterProfileDto(
            history = StubCharacter.HISTORY,
            manner = "   ",
            name = StubCharacter.NAME,
            personality = StubCharacter.PERSONALITY,
        )

        val profile = dto.toModel()

        assertEquals(expected = StubCharacter.stubProfile(manner = ""), actual = profile)
    }
}
