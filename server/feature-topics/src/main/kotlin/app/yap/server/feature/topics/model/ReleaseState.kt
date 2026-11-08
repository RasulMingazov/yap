package app.yap.server.feature.topics.model

import java.time.LocalDate
import java.time.ZoneId

enum class ReleaseState {
    DRAFT,
    PUBLISHED,
    TODAY,
    OUT,
    ;

    companion object {

        val MOSCOW: ZoneId = ZoneId.of("Europe/Moscow")

        fun of(
            status: TopicStatus,
            releaseDate: LocalDate,
            today: LocalDate,
        ): ReleaseState = when (status) {
            TopicStatus.DRAFT -> DRAFT
            TopicStatus.PUBLISHED -> when {
                releaseDate.isAfter(today) -> PUBLISHED
                releaseDate.isBefore(today) -> OUT
                else -> TODAY
            }
        }
    }
}
