package app.yap.admin.feature.levels.data.mapper

import app.yap.admin.feature.levels.domain.StubLevel
import app.yap.contract.admin.LevelDto
import app.yap.contract.admin.LevelInputDto
import app.yap.contract.admin.LevelSettingsDto
import app.yap.contract.admin.RatingRangeDto

internal object StubLevelDto {

    const val CODE = "B1"
    const val IDIOMS = "RARELY"

    fun stubLevelDto(
        code: String = CODE,
        range: RatingRangeDto? = stubRangeDto(),
        settings: LevelSettingsDto? = stubSettingsDto(),
    ): LevelDto = LevelDto(
        code = code,
        range = range,
        settings = settings,
    )

    fun stubInputDto(
        code: String = CODE,
        settings: LevelSettingsDto = stubSettingsDto(),
    ): LevelInputDto = LevelInputDto(
        code = code,
        settings = settings,
    )

    fun stubRangeDto(
        from: Int = StubLevel.RANGE_FROM,
        to: Int? = StubLevel.RANGE_TO,
    ): RatingRangeDto = RatingRangeDto(
        from = from,
        to = to,
    )

    fun stubSettingsDto(
        block: String = StubLevel.BLOCK,
        idioms: String = IDIOMS,
        maxWords: Int = StubLevel.MAX_WORDS,
        ratingFrom: Int? = StubLevel.RANGE_FROM,
        vocabulary: String = StubLevel.VOCABULARY,
    ): LevelSettingsDto = LevelSettingsDto(
        block = block,
        idioms = idioms,
        maxWords = maxWords,
        ratingFrom = ratingFrom,
        vocabulary = vocabulary,
    )
}
