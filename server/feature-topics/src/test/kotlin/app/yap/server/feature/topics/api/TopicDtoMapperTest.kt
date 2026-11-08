package app.yap.server.feature.topics.api

import app.yap.contract.admin.InterlocutorDto
import app.yap.server.feature.characters.model.StubCharacter
import app.yap.server.feature.topics.model.Interlocutor
import app.yap.server.feature.topics.model.StubTopic
import io.ktor.server.plugins.BadRequestException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

internal class TopicDtoMapperTest {

    @Test
    fun `GIVEN texts with spaces around WHEN mapping the content THEN every text stays as it is`() {
        val dto = StubTopicDto.stubContentDto(
            description = " Order a coffee. ",
            interlocutor = InterlocutorDto.GuestDto(description = " A tourist. "),
            openingLine = StubTopicDto.stubOpeningLineDto(english = " Hello! ", translation = " Привет! "),
            role = " A barista. ",
            setting = " A cafe. ",
            steps = listOf(
                StubTopicDto.stubStepDto(outcome = " Ordered. ", passWhen = " Names it. ", task = " Order. "),
            ),
            title = " At the cafe ",
        )

        val content = dto.toModel()

        assertEquals(
            expected = StubTopic.stubContent(
                description = " Order a coffee. ",
                interlocutor = Interlocutor.Guest(description = " A tourist. "),
                openingLine = StubTopic.stubOpeningLine(english = " Hello! ", translation = " Привет! "),
                role = " A barista. ",
                setting = " A cafe. ",
                steps = listOf(
                    StubTopic.stubStep(outcome = " Ordered. ", passWhen = " Names it. ", task = " Order. "),
                ),
                title = " At the cafe ",
            ),
            actual = content,
        )
    }

    @Test
    fun `GIVEN a filled step and an empty step after it WHEN mapping the content THEN both steps stay`() {
        val dto = StubTopicDto.stubContentDto(
            steps = listOf(
                StubTopicDto.stubStepDto(),
                StubTopicDto.stubStepDto(outcome = "", passWhen = "", task = ""),
            ),
        )

        val content = dto.toModel()

        assertEquals(
            expected = listOf(StubTopic.stubStep(), StubTopic.stubStep(outcome = "", passWhen = "", task = "")),
            actual = content.steps,
        )
    }

    @Test
    fun `GIVEN a character WHEN mapping the content THEN the interlocutor is that character of the cast`() {
        val dto = StubTopicDto.stubContentDto(
            interlocutor = InterlocutorDto.CastDto(characterId = StubTopicDto.CHARACTER_ID),
        )

        val content = dto.toModel()

        assertEquals(
            expected = Interlocutor.Cast(characterId = StubCharacter.CHARACTER_ID),
            actual = content.interlocutor,
        )
    }

    @Test
    fun `GIVEN no interlocutor WHEN mapping the content THEN the content has no interlocutor`() {
        val dto = StubTopicDto.stubContentDto(interlocutor = null)

        val content = dto.toModel()

        assertNull(content.interlocutor)
    }

    @Test
    fun `GIVEN a release date that does not parse WHEN mapping the content THEN it fails with a bad request`() {
        val dto = StubTopicDto.stubContentDto(releaseDate = "24 October")

        assertFailsWith<BadRequestException> { dto.toModel() }
    }

    @Test
    fun `GIVEN a character that is not an identifier WHEN mapping the content THEN it fails with a bad request`() {
        val dto = StubTopicDto.stubContentDto(
            interlocutor = InterlocutorDto.CastDto(characterId = "sam"),
        )

        assertFailsWith<BadRequestException> { dto.toModel() }
    }

    @Test
    fun `GIVEN a topic with a character WHEN mapping it to the DTO THEN the DTO holds every field`() {
        val topic = StubTopic.stubTopic()

        val dto = topic.toDto()

        assertEquals(expected = StubTopicDto.stubTopicDto(), actual = dto)
    }

    @Test
    fun `GIVEN a guest WHEN mapping the topic to the DTO THEN the interlocutor is a guest`() {
        val topic = StubTopic.stubTopic(
            content = StubTopic.stubContent(interlocutor = Interlocutor.Guest(description = "A tourist.")),
        )

        val dto = topic.toDto()

        assertEquals(
            expected = InterlocutorDto.GuestDto(description = "A tourist."),
            actual = dto.content.interlocutor,
        )
    }

    @Test
    fun `GIVEN no interlocutor WHEN mapping the topic to the DTO THEN the DTO has no interlocutor`() {
        val topic = StubTopic.stubTopic(
            content = StubTopic.stubContent(interlocutor = null),
        )

        val dto = topic.toDto()

        assertNull(dto.content.interlocutor)
    }
}
