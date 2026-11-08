package app.yap.server.feature.topics.persistence

import app.yap.server.core.database.Database
import app.yap.server.feature.characters.model.CharacterId
import app.yap.server.feature.topics.model.Interlocutor
import app.yap.server.feature.topics.model.OpeningLine
import app.yap.server.feature.topics.model.Step
import app.yap.server.feature.topics.model.Topic
import app.yap.server.feature.topics.model.TopicContent
import app.yap.server.feature.topics.model.TopicId
import app.yap.server.feature.topics.model.TopicStatus
import kotlin.uuid.Uuid
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.lowerCase
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update

class DatabaseTopicRepository(
    private val database: Database,
) : TopicRepository {

    override suspend fun create(content: TopicContent): Topic {
        val topic = Topic(
            content = content,
            status = TopicStatus.DRAFT,
            topicId = TopicId(Uuid.random()),
        )
        database.transaction {
            TopicsTable.insert { statement ->
                statement[TopicsTable.status] = topic.status
                statement[TopicsTable.topicId] = topic.topicId.value
                statement.write(content)
            }
            insertSteps(topic)
        }
        return topic
    }

    override suspend fun find(topicId: TopicId): Topic? = database.transaction {
        TopicsTable
            .selectAll()
            .where { TopicsTable.topicId eq topicId.value }
            .singleOrNull()
            ?.toTopic(steps = readSteps(topicId.value))
    }

    override suspend fun list(): List<Topic> = database.transaction {
        val stepsByTopic = TopicStepsTable
            .selectAll()
            .orderBy(TopicStepsTable.position)
            .groupBy({ row -> row[TopicStepsTable.topicId] }, { row -> row.toStep() })
        TopicsTable
            .selectAll()
            .orderBy(
                TopicsTable.releaseDate to SortOrder.ASC,
                TopicsTable.title.lowerCase() to SortOrder.ASC,
            )
            .map { row -> row.toTopic(steps = stepsByTopic[row[TopicsTable.topicId]].orEmpty()) }
    }

    override suspend fun update(topic: Topic): TopicWrite = try {
        database.transaction {
            maxAttempts = 1
            val updatedRows = TopicsTable.update(
                where = { (TopicsTable.topicId eq topic.topicId.value) and (TopicsTable.status eq topic.status) },
            ) { statement ->
                statement.write(topic.content)
            }
            when {
                updatedRows > 0 -> {
                    TopicStepsTable.deleteWhere { TopicStepsTable.topicId eq topic.topicId.value }
                    insertSteps(topic)
                    TopicWrite.Written(topic)
                }

                isStored(topic.topicId.value) -> TopicWrite.StaleStatus
                else -> TopicWrite.NotFound
            }
        }
    } catch (exception: ExposedSQLException) {
        if (exception.sqlState != UNIQUE_VIOLATION) throw exception
        TopicWrite.DateTaken
    }
}

private const val UNIQUE_VIOLATION = "23505"

private fun JdbcTransaction.isStored(topicId: Uuid): Boolean = !TopicsTable
    .selectAll()
    .where { TopicsTable.topicId eq topicId }
    .empty()

private fun JdbcTransaction.readSteps(topicId: Uuid): List<Step> = TopicStepsTable
    .selectAll()
    .where { TopicStepsTable.topicId eq topicId }
    .orderBy(TopicStepsTable.position)
    .map { row -> row.toStep() }

private fun ResultRow.toTopic(steps: List<Step>): Topic = Topic(
    content = TopicContent(
        description = this[TopicsTable.description],
        durationMinutes = this[TopicsTable.durationMinutes],
        interlocutor = toInterlocutor(),
        openingLine = OpeningLine(
            english = this[TopicsTable.openingEnglish],
            translation = this[TopicsTable.openingTranslation],
        ),
        releaseDate = this[TopicsTable.releaseDate],
        role = this[TopicsTable.role],
        setting = this[TopicsTable.setting],
        steps = steps,
        title = this[TopicsTable.title],
    ),
    status = this[TopicsTable.status],
    topicId = TopicId(this[TopicsTable.topicId]),
)

private fun ResultRow.toInterlocutor(): Interlocutor? {
    val characterId = this[TopicsTable.characterId]
    val guest = this[TopicsTable.guest]
    return when {
        characterId != null -> Interlocutor.Cast(characterId = CharacterId(characterId))
        guest != null -> Interlocutor.Guest(description = guest)
        else -> null
    }
}

private fun ResultRow.toStep(): Step = Step(
    outcome = this[TopicStepsTable.outcome],
    passWhen = this[TopicStepsTable.passWhen],
    task = this[TopicStepsTable.task],
)

private fun UpdateBuilder<*>.write(content: TopicContent) {
    this[TopicsTable.characterId] = (content.interlocutor as? Interlocutor.Cast)?.characterId?.value
    this[TopicsTable.description] = content.description
    this[TopicsTable.durationMinutes] = content.durationMinutes
    this[TopicsTable.guest] = (content.interlocutor as? Interlocutor.Guest)?.description
    this[TopicsTable.openingEnglish] = content.openingLine.english
    this[TopicsTable.openingTranslation] = content.openingLine.translation
    this[TopicsTable.releaseDate] = requireNotNull(content.releaseDate) { "A stored topic has a release date" }
    this[TopicsTable.role] = content.role
    this[TopicsTable.setting] = content.setting
    this[TopicsTable.title] = content.title
}

private fun JdbcTransaction.insertSteps(topic: Topic) {
    TopicStepsTable.batchInsert(topic.content.steps.withIndex()) { (position, step) ->
        this[TopicStepsTable.outcome] = step.outcome
        this[TopicStepsTable.passWhen] = step.passWhen
        this[TopicStepsTable.position] = position
        this[TopicStepsTable.task] = step.task
        this[TopicStepsTable.topicId] = topic.topicId.value
    }
}
