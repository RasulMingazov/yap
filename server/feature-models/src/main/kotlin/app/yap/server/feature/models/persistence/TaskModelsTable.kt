package app.yap.server.feature.models.persistence

import app.yap.server.core.llm.ModelProviderId
import app.yap.server.feature.models.model.ModelTask
import org.jetbrains.exposed.v1.core.Table

internal object TaskModelsTable : Table("task_models") {

    val model = text("model")
    val provider = enumerationByName<ModelProviderId>("provider", length = ENUM_NAME_LENGTH)
    val task = enumerationByName<ModelTask>("task", length = ENUM_NAME_LENGTH)

    override val primaryKey = PrimaryKey(task)
}

private const val ENUM_NAME_LENGTH = 32
