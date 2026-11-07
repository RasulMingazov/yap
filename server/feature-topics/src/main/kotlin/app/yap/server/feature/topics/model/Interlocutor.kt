package app.yap.server.feature.topics.model

import app.yap.server.feature.characters.model.CharacterId

sealed interface Interlocutor {

    data class Cast(val characterId: CharacterId) : Interlocutor

    data class Guest(val description: String) : Interlocutor
}
