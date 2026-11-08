package app.yap.server.feature.topics.persistence

import app.yap.server.feature.topics.model.TopicStatus
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.date

internal object TopicsTable : Table("topics") {

    val characterId = uuid("character_id").nullable()
    val description = text("description")
    val durationMinutes = integer("duration_minutes").nullable()
    val guest = text("guest").nullable()
    val openingEnglish = text("opening_english")
    val openingTranslation = text("opening_translation")
    val releaseDate = date("release_date")
    val role = text("role")
    val setting = text("setting")
    val status = enumerationByName<TopicStatus>("status", length = STATUS_LENGTH)
    val title = text("title")
    val topicId = uuid("topic_id")

    override val primaryKey = PrimaryKey(topicId)
}

private const val STATUS_LENGTH = 16
