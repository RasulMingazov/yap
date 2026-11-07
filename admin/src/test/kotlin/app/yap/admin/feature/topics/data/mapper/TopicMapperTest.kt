package app.yap.admin.feature.topics.data.mapper

import app.yap.admin.feature.topics.domain.Interlocutor
import app.yap.admin.feature.topics.domain.StubTopic
import app.yap.contract.admin.InterlocutorDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class TopicMapperTest {

    @Test
    fun `GIVEN a topic with a character WHEN mapping it THEN the topic holds every field`() {
        val dto = StubTopicDto.stubTopicDto()

        val topic = dto.toDomain()

        assertEquals(expected = StubTopic.stubTopic(), actual = topic)
    }

    @Test
    fun `GIVEN a guest WHEN mapping the topic THEN the interlocutor is that guest`() {
        val dto = StubTopicDto.stubTopicDto(
            content = StubTopicDto.stubContentDto(interlocutor = InterlocutorDto.GuestDto(description = "A tourist.")),
        )

        val topic = dto.toDomain()

        assertEquals(expected = Interlocutor.Guest(description = "A tourist."), actual = topic.content.interlocutor)
    }

    @Test
    fun `GIVEN no interlocutor WHEN mapping the topic THEN the topic has no interlocutor`() {
        val dto = StubTopicDto.stubTopicDto(content = StubTopicDto.stubContentDto(interlocutor = null))

        val topic = dto.toDomain()

        assertNull(topic.content.interlocutor)
    }

    @Test
    fun `GIVEN no release date WHEN mapping the topic THEN the topic has no release date`() {
        val dto = StubTopicDto.stubTopicDto(content = StubTopicDto.stubContentDto(releaseDate = null))

        val topic = dto.toDomain()

        assertNull(topic.content.releaseDate)
    }

    @Test
    fun `GIVEN no duration WHEN mapping the topic THEN the topic has no duration`() {
        val dto = StubTopicDto.stubTopicDto(content = StubTopicDto.stubContentDto(durationMinutes = null))

        val topic = dto.toDomain()

        assertNull(topic.content.durationMinutes)
    }

    @Test
    fun `GIVEN a guest WHEN mapping the content THEN the DTO holds that guest`() {
        val content = StubTopic.stubContent(interlocutor = Interlocutor.Guest(description = "A tourist."))

        val dto = content.toDto()

        assertEquals(expected = InterlocutorDto.GuestDto(description = "A tourist."), actual = dto.interlocutor)
    }

    @Test
    fun `GIVEN no date, duration or interlocutor WHEN mapping the content THEN the DTO has none of them`() {
        val content = StubTopic.stubContent(durationMinutes = null, interlocutor = null, releaseDate = null)

        val dto = content.toDto()

        assertEquals(
            expected = StubTopicDto.stubContentDto(durationMinutes = null, interlocutor = null, releaseDate = null),
            actual = dto,
        )
    }
}
