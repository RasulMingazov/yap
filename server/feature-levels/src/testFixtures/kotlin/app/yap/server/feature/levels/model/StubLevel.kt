package app.yap.server.feature.levels.model

object StubLevel {

    const val RATING_FROM = 1050

    fun stubSettings(
        block: String = "Speak in short, simple sentences. Avoid phrasal verbs.",
        idioms: Idioms = Idioms.RARELY,
        maxWords: Int = 16,
        ratingFrom: Int? = RATING_FROM,
        vocabulary: String = "B1",
    ): LevelSettings = LevelSettings(
        block = block,
        idioms = idioms,
        maxWords = maxWords,
        ratingFrom = ratingFrom,
        vocabulary = vocabulary,
    )

    fun stubTable(
        a1: LevelSettings = stubSettings(ratingFrom = 600),
        a2: LevelSettings = stubSettings(ratingFrom = 900),
        b1: LevelSettings = stubSettings(),
        b2: LevelSettings = stubSettings(ratingFrom = 1200),
        c1: LevelSettings = stubSettings(ratingFrom = 1350),
        c2: LevelSettings = stubSettings(ratingFrom = 1500),
        noRating: LevelSettings = stubSettings(ratingFrom = null),
    ): List<LevelEntry> = listOf(
        LevelEntry(code = LevelCode.A1, settings = a1),
        LevelEntry(code = LevelCode.A2, settings = a2),
        LevelEntry(code = LevelCode.B1, settings = b1),
        LevelEntry(code = LevelCode.B2, settings = b2),
        LevelEntry(code = LevelCode.C1, settings = c1),
        LevelEntry(code = LevelCode.C2, settings = c2),
        LevelEntry(code = LevelCode.NO_RATING, settings = noRating),
    )
}
