package app.yap.server.feature.characters.api

import app.yap.contract.admin.CharacterDto
import app.yap.contract.admin.CharacterProfileDto
import app.yap.contract.admin.CharacterSummaryDto
import app.yap.server.feature.characters.model.Character
import app.yap.server.feature.characters.model.CharacterProfile
import app.yap.server.feature.characters.model.CharacterSummary

internal fun Character.toDto(): CharacterDto = CharacterDto(
    characterId = characterId.value.toString(),
    profile = profile.toDto(),
)

internal fun CharacterSummary.toDto(): CharacterSummaryDto = CharacterSummaryDto(
    characterId = character.characterId.value.toString(),
    profile = character.profile.toDto(),
    topicCount = topicCount,
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
