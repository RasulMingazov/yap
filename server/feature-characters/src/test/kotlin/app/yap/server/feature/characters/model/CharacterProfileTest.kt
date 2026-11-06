package app.yap.server.feature.characters.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class CharacterProfileTest {

    @Test
    fun `GIVEN fields with spaces around WHEN checking the profile THEN the fields are trimmed`() {
        val profile = StubCharacter.stubProfile(
            history = " Moved here. ",
            manner = " Casual. ",
            name = " Sam ",
            personality = " Friendly. ",
        )

        val checked = profile.trimmedIfFilled()

        assertEquals(
            expected = StubCharacter.stubProfile(
                history = "Moved here.",
                manner = "Casual.",
                name = "Sam",
                personality = "Friendly.",
            ),
            actual = checked,
        )
    }

    @Test
    fun `GIVEN a field with spaces only WHEN checking the profile THEN there is no profile`() {
        val profile = StubCharacter.stubProfile(manner = "   ")

        val checked = profile.trimmedIfFilled()

        assertNull(checked)
    }
}
