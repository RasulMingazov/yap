package app.yap.server.feature.topics.model

import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

private val MOSCOW: ZoneId = ZoneId.of("Europe/Moscow")

internal fun moscowToday(clock: Clock): LocalDate = LocalDate.now(clock.withZone(MOSCOW))
