package app.yap.server.feature.prompts.persistence

import app.yap.server.feature.prompts.model.PromptKey
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.timestamp

internal object PromptVersionsTable : Table("prompt_versions") {

    val number = integer("number")
    val promptKey = enumerationByName<PromptKey>("prompt_key", length = KEY_LENGTH)
    val savedAt = timestamp("saved_at")
    val text = text("text")

    override val primaryKey = PrimaryKey(promptKey, number)
}

internal object CurrentPromptsTable : Table("current_prompts") {

    val number = integer("number")
    val promptKey = enumerationByName<PromptKey>("prompt_key", length = KEY_LENGTH)

    override val primaryKey = PrimaryKey(promptKey)
}

private const val KEY_LENGTH = 32
