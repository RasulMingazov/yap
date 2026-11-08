package app.yap.server.feature.characters.api

import app.yap.contract.admin.CharacterProfileDto
import app.yap.server.feature.characters.model.StubCharacter
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CharacterDtoMapperTest {

    @Test
    fun `GIVEN spaces around the fields WHEN mapping the profile THEN the fields stay as they are`() {
        val dto = CharacterProfileDto(
            history = " Moved here. ",
            manner = " Casual. ",
            name = " Sam ",
            personality = " Friendly. ",
        )

        val profile = dto.toModel()

        assertEquals(
            expected = StubCharacter.stubProfile(
                history = " Moved here. ",
                manner = " Casual. ",
                name = " Sam ",
                personality = " Friendly. ",
            ),
            actual = profile,
        )
    }
}
