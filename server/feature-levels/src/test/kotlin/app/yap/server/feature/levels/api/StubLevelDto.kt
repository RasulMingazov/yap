package app.yap.server.feature.levels.api

import app.yap.contract.admin.LevelInputDto
import app.yap.contract.admin.LevelSettingsDto
import app.yap.server.feature.levels.model.StubLevel

internal object StubLevelDto {

    fun stubInputDto(
        code: String = "B1",
        settings: LevelSettingsDto = stubSettingsDto(),
    ): LevelInputDto = LevelInputDto(
        code = code,
        settings = settings,
    )

    fun stubSettingsDto(
        block: String = StubLevel.BLOCK,
        idioms: String = "RARELY",
        maxWords: Int = StubLevel.MAX_WORDS,
        ratingFrom: Int? = StubLevel.RATING_FROM,
        vocabulary: String = StubLevel.VOCABULARY,
    ): LevelSettingsDto = LevelSettingsDto(
        block = block,
        idioms = idioms,
        maxWords = maxWords,
        ratingFrom = ratingFrom,
        vocabulary = vocabulary,
    )
}
