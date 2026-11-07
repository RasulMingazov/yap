package app.yap.server.feature.characters.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CharacterProfileTest {

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
