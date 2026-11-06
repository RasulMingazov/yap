package app.yap.admin.feature.characters.domain

internal sealed interface SaveCharacterResult {

    data object Failed : SaveCharacterResult

    data object Invalid : SaveCharacterResult

    data class Saved(val character: Character) : SaveCharacterResult
}
