package app.yap.server.feature.topics.persistence

import org.jetbrains.exposed.v1.core.Table

internal object TopicStepsTable : Table("topic_steps") {

    val outcome = text("outcome")
    val passWhen = text("pass_when")
    val position = integer("position")
    val task = text("task")
    val topicId = uuid("topic_id")

    override val primaryKey = PrimaryKey(topicId, position)
}
