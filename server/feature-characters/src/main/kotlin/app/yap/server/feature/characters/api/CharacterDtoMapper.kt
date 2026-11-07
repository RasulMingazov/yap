package app.yap.server.feature.characters.api

import app.yap.contract.admin.CharacterDto
import app.yap.contract.admin.CharacterProfileDto
import app.yap.server.feature.characters.model.Character
import app.yap.server.feature.characters.model.CharacterProfile

internal fun Character.toDto(): CharacterDto = CharacterDto(
    characterId = characterId.value.toString(),
    profile = profile.toDto(),
)

private fun CharacterProfile.toDto(): CharacterProfileDto = CharacterProfileDto(
    history = history,
    manner = manner,
    name = name,
    personality = personality,
)

internal fun CharacterProfileDto.toModel(): CharacterProfile = CharacterProfile(
    history = history.trim(),
    manner = manner.trim(),
    name = name.trim(),
    personality = personality.trim(),
)
