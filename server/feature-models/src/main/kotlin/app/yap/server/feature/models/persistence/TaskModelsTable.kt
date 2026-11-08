package app.yap.server.feature.models.persistence

import org.jetbrains.exposed.v1.core.Table

internal object TaskModelsTable : Table("task_models") {

    val model = text("model")
    val provider = text("provider")
    val task = text("task")

    override val primaryKey = PrimaryKey(task)
}
