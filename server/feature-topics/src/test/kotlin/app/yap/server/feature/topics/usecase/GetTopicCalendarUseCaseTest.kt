package app.yap.server.feature.topics.usecase

import app.yap.server.feature.characters.model.StubCharacter
import app.yap.server.feature.characters.persistence.StubCharacterRepository
import app.yap.server.feature.characters.usecase.GetCharacterUseCase
import app.yap.server.feature.characters.usecase.GetCharactersUseCase
import app.yap.server.feature.topics.model.LedBy
import app.yap.server.feature.topics.model.TopicCalendarRule
import app.yap.server.feature.topics.model.TopicDraftRule
import app.yap.server.feature.topics.model.TopicPublishRule
import app.yap.server.feature.topics.persistence.StubTopicRepository
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

internal class GetTopicCalendarUseCaseTest {

    @Test
    fun `GIVEN 22 30 UTC and a topic led by a character WHEN reading THEN the day is Moscow and the row has the name`() =
        runTest {
            val env = Environment()

            val calendar = env.useCase()

            assertEquals(expected = LocalDate.of(2026, 10, 23), actual = calendar.rows.first().date)
            assertEquals(
                expected = LedBy.Cast(StubCharacter.NAME),
                actual = calendar.rows.mapNotNull { row -> row.topic }.single().ledBy,
            )
        }

    private class Environment {

        val characterRepository = StubCharacterRepository()
        val topicRepository = StubTopicRepository()
        val useCase = GetTopicCalendarUseCase(
            clock = Clock.fixed(Instant.parse("2026-10-22T22:30:00Z"), ZoneOffset.UTC),
            getCharactersUseCase = GetCharactersUseCase(characterRepository = characterRepository),
            topicCalendarRule = TopicCalendarRule(
                topicPublishRule = TopicPublishRule(
                    topicDraftRule = TopicDraftRule(
                        getCharacterUseCase = GetCharacterUseCase(characterRepository = characterRepository),
                    ),
                ),
            ),
            topicRepository = topicRepository,
        )
    }
}
