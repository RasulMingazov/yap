package app.yap.admin.feature.characters.presentation

import kotlin.test.Test
import kotlin.test.assertEquals

internal class TopicCountTextTest {

    @Test
    fun `GIVEN no topics WHEN writing the count THEN it says 0 тем`() {
        val text = TopicCountText(0)

        assertEquals(expected = "0 тем", actual = text)
    }

    @Test
    fun `GIVEN 1 topic WHEN writing the count THEN it says 1 тема`() {
        val text = TopicCountText(1)

        assertEquals(expected = "1 тема", actual = text)
    }

    @Test
    fun `GIVEN 2 topics WHEN writing the count THEN it says 2 темы`() {
        val text = TopicCountText(2)

        assertEquals(expected = "2 темы", actual = text)
    }

    @Test
    fun `GIVEN 5 topics WHEN writing the count THEN it says 5 тем`() {
        val text = TopicCountText(5)

        assertEquals(expected = "5 тем", actual = text)
    }

    @Test
    fun `GIVEN 11 topics WHEN writing the count THEN it says 11 тем`() {
        val text = TopicCountText(11)

        assertEquals(expected = "11 тем", actual = text)
    }

    @Test
    fun `GIVEN 12 topics WHEN writing the count THEN it says 12 тем`() {
        val text = TopicCountText(12)

        assertEquals(expected = "12 тем", actual = text)
    }

    @Test
    fun `GIVEN 21 topics WHEN writing the count THEN it says 21 тема`() {
        val text = TopicCountText(21)

        assertEquals(expected = "21 тема", actual = text)
    }

    @Test
    fun `GIVEN 22 topics WHEN writing the count THEN it says 22 темы`() {
        val text = TopicCountText(22)

        assertEquals(expected = "22 темы", actual = text)
    }

    @Test
    fun `GIVEN 111 topics WHEN writing the count THEN it says 111 тем`() {
        val text = TopicCountText(111)

        assertEquals(expected = "111 тем", actual = text)
    }
}
