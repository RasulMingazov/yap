package app.yap.server.feature.prompts.persistence

import app.yap.server.core.database.Database
import app.yap.server.feature.prompts.model.Prompt
import app.yap.server.feature.prompts.model.PromptKey
import app.yap.server.feature.prompts.model.PromptVersion
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.max
import org.jetbrains.exposed.v1.javatime.CurrentTimestamp
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.upsert

class DatabasePromptRepository(
    private val database: Database,
) : PromptRepository {

    override suspend fun get(promptKey: PromptKey): Prompt = database.transaction {
        readPrompt(promptKey)
    }

    override suspend fun restore(promptKey: PromptKey, number: Int): Prompt? = database.transaction {
        if (hasVersion(promptKey, number)) {
            CurrentPromptsTable.update(
                where = { CurrentPromptsTable.promptKey eq promptKey },
            ) { statement ->
                statement[CurrentPromptsTable.number] = number
            }
            readPrompt(promptKey)
        } else {
            null
        }
    }

    override suspend fun save(promptKey: PromptKey, text: String): Prompt = database.transaction {
        val number = nextNumber(promptKey)
        PromptVersionsTable.insert { statement ->
            statement[PromptVersionsTable.number] = number
            statement[PromptVersionsTable.promptKey] = promptKey
            statement[PromptVersionsTable.savedAt] = CurrentTimestamp
            statement[PromptVersionsTable.text] = text
        }
        CurrentPromptsTable.upsert { statement ->
            statement[CurrentPromptsTable.number] = number
            statement[CurrentPromptsTable.promptKey] = promptKey
        }
        readPrompt(promptKey)
    }
}

private fun JdbcTransaction.hasVersion(promptKey: PromptKey, number: Int): Boolean = !PromptVersionsTable
    .selectAll()
    .where { (PromptVersionsTable.promptKey eq promptKey) and (PromptVersionsTable.number eq number) }
    .empty()

private fun JdbcTransaction.nextNumber(promptKey: PromptKey): Int {
    val highest = PromptVersionsTable.number.max()
    val highestNumber = PromptVersionsTable
        .select(highest)
        .where { PromptVersionsTable.promptKey eq promptKey }
        .single()[highest]
    return (highestNumber ?: 0) + 1
}

private fun JdbcTransaction.readPrompt(promptKey: PromptKey): Prompt {
    val versions = PromptVersionsTable
        .selectAll()
        .where { PromptVersionsTable.promptKey eq promptKey }
        .orderBy(PromptVersionsTable.number, SortOrder.DESC)
        .map { row -> row.toVersion() }
    val currentNumber = CurrentPromptsTable
        .selectAll()
        .where { CurrentPromptsTable.promptKey eq promptKey }
        .singleOrNull()
        ?.get(CurrentPromptsTable.number)
    return Prompt(
        current = versions.firstOrNull { version -> version.number == currentNumber },
        promptKey = promptKey,
        versions = versions,
    )
}

private fun ResultRow.toVersion(): PromptVersion = PromptVersion(
    number = this[PromptVersionsTable.number],
    savedAt = this[PromptVersionsTable.savedAt],
    text = this[PromptVersionsTable.text],
)
