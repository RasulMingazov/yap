package app.yap.server.feature.levels.model

internal object RatingRanges {

    fun levelsOf(saved: List<LevelEntry>): List<Level> {
        val settingsByCode = saved.associate { entry -> entry.code to entry.settings }
        return LevelCode.entries.map { code ->
            Level(
                code = code,
                range = rangeOf(code, settingsByCode),
                settings = settingsByCode[code],
            )
        }
    }

    private fun rangeOf(code: LevelCode, settingsByCode: Map<LevelCode, LevelSettings>): RatingRange? {
        val from = settingsByCode[code]?.ratingFrom ?: return null
        val nextFrom = code.nextRated?.let { next -> settingsByCode[next]?.ratingFrom }
        return RatingRange(
            from = from,
            to = nextFrom?.let { ratingFrom -> ratingFrom - 1 },
        )
    }
}
