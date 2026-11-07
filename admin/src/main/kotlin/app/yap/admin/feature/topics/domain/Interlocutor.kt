package app.yap.admin.feature.topics.domain

import app.yap.admin.feature.characters.domain.CharacterId

internal sealed interface Interlocutor {

    data class Cast(val characterId: CharacterId) : Interlocutor

    data class Guest(val description: String) : Interlocutor
}
