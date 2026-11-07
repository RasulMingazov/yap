package app.yap.server.feature.levels.usecase

import app.yap.server.feature.levels.model.LevelCode
import app.yap.server.feature.levels.persistence.LevelRepository

class GetLevelRulesUseCase(
    private val levelRepository: LevelRepository,
) {

    suspend operator fun invoke(code: LevelCode): String? = levelRepository
        .list()
        .find { entry -> entry.code == code }
        ?.settings
        ?.block
}
