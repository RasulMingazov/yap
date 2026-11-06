package app.yap.server.feature.characters.persistence

import app.yap.server.core.database.Database
import app.yap.server.feature.characters.model.Character
import app.yap.server.feature.characters.model.CharacterId
import app.yap.server.feature.characters.model.CharacterProfile
import kotlin.uuid.Uuid
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.lowerCase
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update

class ExposedCharacterRepository(
    private val database: Database,
) : CharacterRepository {

    override suspend fun create(profile: CharacterProfile): Character {
        val character = Character(
            characterId = CharacterId(Uuid.random()),
            profile = profile,
        )
        database.transaction {
            CharactersTable.insert { statement ->
                statement[CharactersTable.characterId] = character.characterId.value
                statement.write(profile)
            }
        }
        return character
    }

    override suspend fun find(characterId: CharacterId): Character? = database.transaction {
        CharactersTable
            .selectAll()
            .where { CharactersTable.characterId eq characterId.value }
            .singleOrNull()
            ?.toCharacter()
    }

    override suspend fun list(): List<Character> = database.transaction {
        CharactersTable
            .selectAll()
            .orderBy(CharactersTable.name.lowerCase())
            .map { row -> row.toCharacter() }
    }

    override suspend fun update(character: Character): Boolean = database.transaction {
        val updatedRows = CharactersTable.update(
            where = { CharactersTable.characterId eq character.characterId.value },
        ) { statement ->
            statement.write(character.profile)
        }
        updatedRows > 0
    }
}

private fun ResultRow.toCharacter(): Character = Character(
    characterId = CharacterId(this[CharactersTable.characterId]),
    profile = CharacterProfile(
        history = this[CharactersTable.history],
        manner = this[CharactersTable.manner],
        name = this[CharactersTable.name],
        personality = this[CharactersTable.personality],
    ),
)

private fun UpdateBuilder<*>.write(profile: CharacterProfile) {
    this[CharactersTable.history] = profile.history
    this[CharactersTable.manner] = profile.manner
    this[CharactersTable.name] = profile.name
    this[CharactersTable.personality] = profile.personality
}
