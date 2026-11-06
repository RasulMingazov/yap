package app.yap.admin.feature.characters.domain.usecase

import app.yap.admin.feature.characters.domain.CharacterId
import app.yap.admin.feature.characters.domain.CharacterProfile
import app.yap.admin.feature.characters.domain.SaveCharacterResult
import app.yap.admin.feature.characters.domain.StubCharacter
import io.github.rasulmingazov.stubcall.StubCall2

internal class StubSaveCharacterUseCase(
    result: SaveCharacterResult = SaveCharacterResult.Saved(StubCharacter.stubCharacter()),
) : SaveCharacterUseCase {

    val invokeCall = StubCall2.returns<CharacterId?, CharacterProfile, SaveCharacterResult>(result)

    override suspend fun invoke(characterId: CharacterId?, profile: CharacterProfile): SaveCharacterResult =
        invokeCall.invoke(characterId, profile)
}
