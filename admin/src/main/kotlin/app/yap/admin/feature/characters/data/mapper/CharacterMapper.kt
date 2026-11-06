package app.yap.admin.feature.characters.data.mapper

import app.yap.admin.feature.characters.domain.Character
import app.yap.admin.feature.characters.domain.CharacterId
import app.yap.admin.feature.characters.domain.CharacterProfile
import app.yap.contract.admin.CharacterDto
import app.yap.contract.admin.CharacterProfileDto

internal fun CharacterDto.toDomain(): Character = Character(
    characterId = CharacterId(characterId),
    profile = profile.toDomain(),
)

private fun CharacterProfileDto.toDomain(): CharacterProfile = CharacterProfile(
    history = history,
    manner = manner,
    name = name,
    personality = personality,
)

internal fun CharacterProfile.toDto(): CharacterProfileDto = CharacterProfileDto(
    history = history,
    manner = manner,
    name = name,
    personality = personality,
)
