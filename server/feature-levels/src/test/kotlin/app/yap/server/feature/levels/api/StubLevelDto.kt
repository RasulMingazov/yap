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

    fun stubInputDtos(): List<LevelInputDto> = listOf(
        stubInputDto(code = "A1", settings = stubSettingsDto(ratingFrom = 600)),
        stubInputDto(code = "A2", settings = stubSettingsDto(ratingFrom = 900)),
        stubInputDto(code = "B1"),
        stubInputDto(code = "B2", settings = stubSettingsDto(ratingFrom = 1200)),
        stubInputDto(code = "C1", settings = stubSettingsDto(ratingFrom = 1350)),
        stubInputDto(code = "C2", settings = stubSettingsDto(ratingFrom = 1500)),
        stubInputDto(code = "NO_RATING", settings = stubSettingsDto(ratingFrom = null)),
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
