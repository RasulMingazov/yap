package app.yap.admin.feature.levels.domain

internal object StubLevel {

    const val BLOCK = "Speak in short, simple sentences."
    const val MAX_WORDS = 16
    const val RANGE_FROM = 1050
    const val RANGE_TO = 1199
    const val VOCABULARY = "B1"

    fun stubLevel(
        code: LevelCode = LevelCode.B1,
        range: RatingRange? = stubRange(),
        settings: LevelSettings? = stubSettings(),
    ): Level = Level(
        code = code,
        range = range,
        settings = settings,
    )

    fun stubLevels(
        range: RatingRange? = stubRange(),
        settings: LevelSettings? = stubSettings(),
    ): List<Level> = LevelCode.entries.map { code ->
        stubLevel(
            code = code,
            range = range,
            settings = if (code == LevelCode.NO_RATING) settings?.copy(ratingFrom = null) else settings,
        )
    }

    fun stubEntry(
        code: LevelCode = LevelCode.B1,
        settings: LevelSettings = stubSettings(),
    ): LevelEntry = LevelEntry(
        code = code,
        settings = settings,
    )

    fun stubInput(
        block: String = BLOCK,
        idioms: Idioms? = Idioms.RARELY,
        maxWords: String = MAX_WORDS.toString(),
        model: String? = null,
        ratingFrom: String = RANGE_FROM.toString(),
        vocabulary: String = VOCABULARY,
    ): LevelInput = LevelInput(
        block = block,
        idioms = idioms,
        maxWords = maxWords,
        model = model,
        ratingFrom = ratingFrom,
        vocabulary = vocabulary,
    )

    fun stubRange(
        from: Int = RANGE_FROM,
        to: Int? = RANGE_TO,
    ): RatingRange = RatingRange(
        from = from,
        to = to,
    )

    fun stubSettings(
        block: String = BLOCK,
        idioms: Idioms = Idioms.RARELY,
        maxWords: Int = MAX_WORDS,
        model: String? = null,
        ratingFrom: Int? = RANGE_FROM,
        vocabulary: String = VOCABULARY,
    ): LevelSettings = LevelSettings(
        block = block,
        idioms = idioms,
        maxWords = maxWords,
        model = model,
        ratingFrom = ratingFrom,
        vocabulary = vocabulary,
    )
}
