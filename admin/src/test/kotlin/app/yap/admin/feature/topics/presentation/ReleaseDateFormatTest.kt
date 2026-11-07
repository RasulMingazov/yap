package app.yap.admin.feature.topics.presentation

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ReleaseDateFormatTest {

    @Test
    fun `GIVEN a Friday in October WHEN formatting short THEN it shows the weekday, the day and the month`() {
        val text = ReleaseDateFormat.short(LocalDate.of(2025, 10, 24))

        assertEquals(expected = "Пт, 24 окт", actual = text)
    }

    @Test
    fun `GIVEN a Monday in May WHEN formatting short THEN the day has no leading zero`() {
        val text = ReleaseDateFormat.short(LocalDate.of(2026, 5, 4))

        assertEquals(expected = "Пн, 4 мая", actual = text)
    }

    @Test
    fun `GIVEN a Friday in October WHEN formatting long THEN it shows the full weekday and the month in genitive`() {
        val text = ReleaseDateFormat.long(LocalDate.of(2025, 10, 24))

        assertEquals(expected = "Пятница, 24 октября", actual = text)
    }
}
