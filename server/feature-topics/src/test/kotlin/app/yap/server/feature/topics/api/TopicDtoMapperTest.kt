package app.yap.server.feature.topics.api

import app.yap.contract.admin.InterlocutorDto
import app.yap.contract.admin.TopicFieldDto
import app.yap.contract.admin.TopicGapDto
import app.yap.contract.admin.TopicStatusDto
import app.yap.server.feature.characters.model.StubCharacter
import app.yap.server.feature.topics.model.Interlocutor
import app.yap.server.feature.topics.model.ReleaseState
import app.yap.server.feature.topics.model.StubTopic
import app.yap.server.feature.topics.model.Topic
import app.yap.server.feature.topics.model.TopicDetails
import app.yap.server.feature.topics.model.TopicField
import app.yap.server.feature.topics.model.TopicGap
import io.ktor.server.plugins.BadRequestException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlinx.serialization.json.Json

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
    fun `GIVEN a draft with a character WHEN mapping its details to the DTO THEN the DTO holds every field`() {
        val details = details()

        val dto = details.toDto()

        assertEquals(expected = StubTopicDto.stubTopicDto(), actual = dto)
    }

    @Test
    fun `GIVEN a guest WHEN mapping the details to the DTO THEN the interlocutor is a guest`() {
        val details = details(
            topic = StubTopic.stubTopic(
                content = StubTopic.stubContent(interlocutor = Interlocutor.Guest(description = "A tourist.")),
            ),
        )

        val dto = details.toDto()

        assertEquals(
            expected = InterlocutorDto.GuestDto(description = "A tourist."),
            actual = dto.content.interlocutor,
        )
    }

    @Test
    fun `GIVEN no interlocutor WHEN mapping the details to the DTO THEN the DTO has no interlocutor`() {
        val details = details(topic = StubTopic.stubTopic(content = StubTopic.stubContent(interlocutor = null)))

        val dto = details.toDto()

        assertNull(dto.content.interlocutor)
    }

    @Test
    fun `GIVEN each release state WHEN mapping it to the DTO THEN each state has its status`() {
        val statuses = ReleaseState.entries.map { state -> state.toDto() }

        assertEquals(
            expected = listOf(
                TopicStatusDto.DRAFT,
                TopicStatusDto.PUBLISHED,
                TopicStatusDto.TODAY,
                TopicStatusDto.OUT,
            ),
            actual = statuses,
        )
    }

    @Test
    fun `GIVEN a missing gap for each field WHEN mapping the details to the DTO THEN each field keeps its name`() {
        val details = details(gaps = TopicField.entries.map { field -> TopicGap.Missing(field) }.toSet())

        val dto = details.toDto()

        assertEquals(
            expected = TopicFieldDto.entries.map { field -> TopicGapDto.MissingDto(field) },
            actual = dto.gaps,
        )
    }

    @Test
    fun `GIVEN the other gaps WHEN mapping the details to the DTO THEN each gap keeps its data`() {
        val details = details(
            gaps = setOf(TopicGap.IncompleteSteps(listOf(3, 5)), TopicGap.PastDate, TopicGap.DateTaken),
        )

        val dto = details.toDto()

        assertEquals(
            expected = listOf(
                TopicGapDto.IncompleteStepsDto(numbers = listOf(3, 5)),
                TopicGapDto.PastDateDto,
                TopicGapDto.DateTakenDto,
            ),
            actual = dto.gaps,
        )
    }

    @Test
    fun `GIVEN every kind of gap WHEN encoding the DTOs to JSON THEN each gap has its type name`() {
        val gaps = listOf(
            TopicGapDto.MissingDto(field = TopicFieldDto.OPENING_LINE),
            TopicGapDto.IncompleteStepsDto(numbers = listOf(3)),
            TopicGapDto.PastDateDto,
            TopicGapDto.DateTakenDto,
        )

        val json = Json.encodeToString(gaps)

        assertEquals(
            expected = Json.parseToJsonElement(
                """
                [
                  {"type": "missing", "field": "OPENING_LINE"},
                  {"type": "incompleteSteps", "numbers": [3]},
                  {"type": "pastDate"},
                  {"type": "dateTaken"}
                ]
                """,
            ),
            actual = Json.parseToJsonElement(json),
        )
    }

    private fun details(
        gaps: Set<TopicGap> = emptySet(),
        state: ReleaseState = ReleaseState.DRAFT,
        topic: Topic = StubTopic.stubTopic(),
    ): TopicDetails = TopicDetails(
        gaps = gaps,
        state = state,
        topic = topic,
    )
}
