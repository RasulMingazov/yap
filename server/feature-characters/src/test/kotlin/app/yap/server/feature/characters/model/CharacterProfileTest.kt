package app.yap.server.feature.characters.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CharacterProfileTest {

    @Test
    fun `GIVEN spaces around the fields WHEN normalizing the profile THEN the fields are trimmed`() {
        val profile = StubCharacter.stubProfile(
            history = " Moved here. ",
            manner = " Casual. ",
            name = " Sam ",
            personality = " Friendly. ",
        )

        val normalized = profile.normalized()

        assertEquals(
            expected = StubCharacter.stubProfile(
                history = "Moved here.",
                manner = "Casual.",
                name = "Sam",
                personality = "Friendly.",
            ),
            actual = normalized,
        )
    }

    @Test
    fun `GIVEN every field has text WHEN checking the profile THEN it is filled`() {
        val profile = StubCharacter.stubProfile()

        assertTrue(profile.isFilled)
    }

    @Test
    fun `GIVEN an empty field WHEN checking the profile THEN it is not filled`() {
        val profile = StubCharacter.stubProfile(manner = "")

        assertFalse(profile.isFilled)
    }
}
