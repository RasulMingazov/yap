package app.yap.server.feature.topics.model

import java.time.LocalDate

enum class ReleaseState {
    DRAFT,
    PUBLISHED,
    TODAY,
    OUT,
    ;

    companion object {

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
