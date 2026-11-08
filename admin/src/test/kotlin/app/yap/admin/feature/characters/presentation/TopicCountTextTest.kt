package app.yap.admin.feature.characters.presentation

import kotlin.test.Test
import kotlin.test.assertEquals

internal class TopicCountTextTest {

    @Test
    fun `GIVEN no topics WHEN writing the count THEN it says 0 тем`() {
        assertEquals(expected = "0 тем", actual = TopicCountText(0))
    }

    @Test
    fun `GIVEN 1 topic WHEN writing the count THEN it says 1 тема`() {
        assertEquals(expected = "1 тема", actual = TopicCountText(1))
    }

    @Test
    fun `GIVEN 2 topics WHEN writing the count THEN it says 2 темы`() {
        assertEquals(expected = "2 темы", actual = TopicCountText(2))
    }

    @Test
    fun `GIVEN 5 topics WHEN writing the count THEN it says 5 тем`() {
        assertEquals(expected = "5 тем", actual = TopicCountText(5))
    }

    @Test
    fun `GIVEN 11 topics WHEN writing the count THEN it says 11 тем`() {
        assertEquals(expected = "11 тем", actual = TopicCountText(11))
    }

    @Test
    fun `GIVEN 12 topics WHEN writing the count THEN it says 12 тем`() {
        assertEquals(expected = "12 тем", actual = TopicCountText(12))
    }

    @Test
    fun `GIVEN 21 topics WHEN writing the count THEN it says 21 тема`() {
        assertEquals(expected = "21 тема", actual = TopicCountText(21))
    }
}
