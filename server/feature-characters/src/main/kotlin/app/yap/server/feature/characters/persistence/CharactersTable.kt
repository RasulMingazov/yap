package app.yap.server.feature.characters.persistence

import org.jetbrains.exposed.v1.core.Table

internal object CharactersTable : Table("characters") {

    val characterId = uuid("character_id")
    val history = text("history")
    val manner = text("manner")
    val name = text("name")
    val personality = text("personality")

    override val primaryKey = PrimaryKey(characterId)
}
