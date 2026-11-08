package app.yap.server.feature.conversation.model

import app.yap.server.feature.levels.model.LevelCode
import app.yap.server.feature.topics.model.StubTopic
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ConversationTest {

    @Test
    fun `GIVEN a topic with an opening line WHEN starting THEN the line is the first one and step 1 is current`() {
        val openingLine = StubTopic.stubOpeningLine()

        val conversation = Conversation.start(
            level = LevelCode.B1,
            openingLine = openingLine,
            topicId = StubTopic.TOPIC_ID,
        )

        val expected = StubConversation.stubConversation(
            currentStep = 1,
            lines = listOf(StubConversation.stubOpeningInterlocutor()),
        )
        assertEquals(expected = expected, actual = conversation)
    }

    @Test
    fun `GIVEN step 1 of 3 WHEN the answer is accepted THEN step 2 is current`() {
        val conversation = StubConversation.stubConversation(currentStep = 1)

        val answered = conversation.answeredWith(Verdict.ACCEPTED)

        assertEquals(expected = 2, actual = answered.currentStep)
    }

    @Test
    fun `GIVEN step 1 WHEN the verdict is continue THEN step 1 stays current`() {
        val conversation = StubConversation.stubConversation(currentStep = 1)

        val answered = conversation.answeredWith(Verdict.CONTINUE)

        assertEquals(expected = 1, actual = answered.currentStep)
    }

    @Test
    fun `GIVEN step 1 WHEN the verdict is unclear THEN step 1 stays current`() {
        val conversation = StubConversation.stubConversation(currentStep = 1)

        val answered = conversation.answeredWith(Verdict.UNCLEAR)

        assertEquals(expected = 1, actual = answered.currentStep)
    }

    @Test
    fun `GIVEN step 1 WHEN the verdict is not accepted THEN step 1 stays current`() {
        val conversation = StubConversation.stubConversation(currentStep = 1)

        val answered = conversation.answeredWith(Verdict.NOT_ACCEPTED)

        assertEquals(expected = 1, actual = answered.currentStep)
    }

    @Test
    fun `GIVEN the last step WHEN the answer is accepted THEN the conversation is finished`() {
        val conversation = StubConversation.stubConversation(currentStep = 3)

        val answered = conversation.answeredWith(Verdict.ACCEPTED)

        assertNull(answered.currentStep)
    }

    @Test
    fun `GIVEN an answer WHEN adding it with a reply THEN the answer comes first and the reply after it`() {
        val conversation = StubConversation.stubConversation()

        val answered = conversation.answeredWith(Verdict.CONTINUE)

        val expected = conversation.lines + listOf(
            StubConversation.stubLearner(step = 1, verdict = Verdict.CONTINUE),
            StubConversation.stubInterlocutor(),
        )
        assertEquals(expected = expected, actual = answered.lines)
    }

    @Test
    fun `GIVEN a continue on step 1 WHEN answering again THEN step 1 holds both answers, the oldest first`() {
        val conversation = StubConversation.stubConversation().answeredWith(Verdict.CONTINUE, answer = "Hi")

        val answered = conversation.answeredWith(Verdict.CONTINUE, answer = "I am Lyosha")

        assertEquals(expected = listOf("Hi", "I am Lyosha"), actual = answered.answersOn(step = 1))
    }

    @Test
    fun `GIVEN an accepted answer on step 1 WHEN reading the answers of step 2 THEN there are none`() {
        val answered = StubConversation.stubConversation().answeredWith(Verdict.ACCEPTED)

        assertEquals(expected = emptyList(), actual = answered.answersOn(step = 2))
    }

    @Test
    fun `GIVEN a reply WHEN reading the last line THEN it is the reply`() {
        val answered = StubConversation.stubConversation().answeredWith(Verdict.CONTINUE)

        assertEquals(expected = StubConversation.stubInterlocutor(), actual = answered.lastLine)
    }

    private fun Conversation.answeredWith(
        verdict: Verdict,
        answer: String = StubConversation.ANSWER,
    ): Conversation = answered(
        answer = answer,
        verdict = verdict,
        reply = StubConversation.stubInterlocutor(),
        stepCount = 3,
    )
}
