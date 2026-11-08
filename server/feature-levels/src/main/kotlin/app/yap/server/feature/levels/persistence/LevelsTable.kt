package app.yap.server.feature.levels.persistence

import app.yap.server.feature.levels.model.Idioms
import app.yap.server.feature.levels.model.LevelCode
import org.jetbrains.exposed.v1.core.Table

internal object LevelsTable : Table("levels") {

    val block = text("block")
    val code = enumerationByName<LevelCode>("code", length = ENUM_NAME_LENGTH)
    val idioms = enumerationByName<Idioms>("idioms", length = ENUM_NAME_LENGTH)
    val maxWords = integer("max_words")
    val model = text("model").nullable()
    val ratingFrom = integer("rating_from").nullable()
    val vocabulary = text("vocabulary")

    override val primaryKey = PrimaryKey(code)
}

private const val ENUM_NAME_LENGTH = 16
